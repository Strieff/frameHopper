package com.FrameHopper.app.ffmpegService;

import com.FrameHopper.app.View.Elements.FXElementsProviders.FXDialogProvider;
import com.FrameHopper.app.View.Elements.Language.Dictionary;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
public class FfmpegService {
    private static final String FFMPEG_PATH = "ffmpeg";
    private static final String FFPROBE_PATH = "ffprobe";
    private static final String FFMPEG_DIR = "ffmpeg/bin/";

    private final boolean isExternalFfmpegPresent;
    @Getter
    private final boolean isFfmpegPresent;

    public FfmpegService() {
        isExternalFfmpegPresent = isExternalFfmpegPresent();
        var isBundledFfmpegPresent = isBundledFfmpegPresent();

        isFfmpegPresent = isExternalFfmpegPresent &&  isBundledFfmpegPresent;

        if(!isFfmpegPresent)
            FXDialogProvider.messageDialog(Dictionary.get("ffmpeg.missing"));
    }

    private boolean isExternalFfmpegPresent() {
        try {
            var p = new ProcessBuilder("ffmpeg", "-version")
                    .redirectErrorStream(true)
                    .start();

            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isBundledFfmpegPresent() {
        if(isExternalFfmpegPresent) return true;

        var exePath = Paths.get(resolveFfmpegPath(FFMPEG_PATH));
        var probePath = Paths.get(resolveFfmpegPath(FFPROBE_PATH));

        var exeOk = Files.exists(exePath) && Files.isRegularFile(exePath) && Files.isExecutable(exePath);
        var probeOk = Files.exists(probePath) && Files.isRegularFile(probePath) && Files.isExecutable(probePath);

        return exeOk && probeOk;
    }

    public byte[] extractFrameBytes(String path, int index) throws InterruptedException, IOException {
        if (index < 0) throw new IllegalArgumentException("Index must be >= 0");

        List<String> cmd = new ArrayList<>();
        cmd.add(resolveFfmpegPath(FFMPEG_PATH));
        cmd.add("-hide_banner");
        cmd.add("-loglevel");
        cmd.add("error");
        cmd.add("-i");
        cmd.add(path);

        // only the selected frame is output
        cmd.add("-vf");
        cmd.add("select=eq(n\\," + index + ")");
        cmd.add("-frames:v");
        cmd.add("1");

        // stream a jpeg image to stdout
        cmd.add("-f");
        cmd.add("image2pipe");
        cmd.add("-vcodec");
        cmd.add("mjpeg");
        cmd.add("pipe:1");

        Process p = new ProcessBuilder(cmd).start();

        // Drain stderr to avoid deadlocks and to keep error context
        var errBuff = new ByteArrayOutputStream();
        var errThread = new Thread(() -> {
            try (InputStream es = p.getErrorStream()) {
                es.transferTo(errBuff);
            } catch (IOException ignored) {}
        }, "ffmpeg-stderr-drain");
        errThread.setDaemon(true);
        errThread.start();

        byte[] outBytes;
        try (InputStream stdOut = new BufferedInputStream(p.getInputStream())) {
            outBytes = stdOut.readAllBytes();
        }

        int exit = p.waitFor();
        errThread.join(1000);

        if (exit != 0 || outBytes.length == 0) {
            String err = errBuff.toString(StandardCharsets.UTF_8);
            throw new IOException("Ffmpeg failed with exit code " + exit +
                    (err.isBlank() ? ". No stderr output." : (". stderr: " + err)));
        }

        return outBytes;
    }

    public VideoInfoDto getVideoInfo(String path) throws IOException, InterruptedException {
        List<String> cmd = List.of(
                resolveFfmpegPath(FFPROBE_PATH),
                "-v", "error",
                "-print_format", "json",
                "-show_format",
                "-show_streams",
                "-select_streams", "v:0",
                path
        );

        Process p = new ProcessBuilder(cmd).start();

        String stdout;
        String stderr;

        try (InputStream out = p.getInputStream();
             InputStream err = p.getErrorStream()) {

            stdout = new String(out.readAllBytes(), StandardCharsets.UTF_8);
            stderr = new String(err.readAllBytes(), StandardCharsets.UTF_8);
        }

        int exitCode = p.waitFor();
        if (exitCode != 0) {
            throw new IOException(
                    "ffprobe failed with exit code " + exitCode +
                            "\nSTDERR:\n" + stderr +
                            "\nSTDOUT:\n" + stdout
            );
        }

        var root = new ObjectMapper().readTree(stdout);

        var stream = root.path("streams").get(0);
        var format = root.path("format");

        int width = stream.path("width").asInt();
        int height = stream.path("height").asInt();

        double durationInSeconds = format.path("duration").asDouble(0.0);

        double frameRate = parseFraction(stream.path("avg_frame_rate").asText());
        if (frameRate <= 0) {
            frameRate = parseFraction(stream.path("r_frame_rate").asText());
        }

        Integer exactFrames = parseIntOrNull(stream.path("nb_frames").asText());
        int totalFrames = exactFrames != null
                ? exactFrames
                : (int) Math.round(durationInSeconds * frameRate);

        return new VideoInfoDto(
                totalFrames,
                durationInSeconds,
                frameRate,
                width,
                height
        );
    }

    private double parseFraction(String value) {
        if (value == null || value.isEmpty() || "N/A".equalsIgnoreCase(value)) return 0.0;

        if (!value.contains("/")) return Double.parseDouble(value);

        var parts = value.split("/");
        var num = Double.parseDouble(parts[0]);
        var den = Double.parseDouble(parts[1]);

        return den == 0 ? 0d : (num / den);
    }

    private Integer parseIntOrNull(String value) {
        if (value == null || value.isEmpty() || "N/A".equalsIgnoreCase(value)) return null;

        return Integer.parseInt(value);
    }

    private String resolveFfmpegPath(String path) {
        if (isExternalFfmpegPresent) return path;

        var os = System.getProperty("os.name").toLowerCase();
        var initialPath = FFMPEG_DIR + path;

        if(os.contains("win")) return initialPath + ".exe";
        return initialPath;
    }


}

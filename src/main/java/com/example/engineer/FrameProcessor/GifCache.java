package com.example.engineer.FrameProcessor;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Iterator;
import java.util.List;

@Component
@Getter
public class GifCache extends Cache{
    @PostConstruct
    private void init(){
        GIF_CACHE = this;
    }

    public void firstLoad(File gifFile){
        try(ImageInputStream stream = ImageIO.createImageInputStream(gifFile)){
            Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");
            if(!readers.hasNext()){
                throw new RuntimeException("No GIF file found");
            }

            ImageReader reader = readers.next();
            reader.setInput(stream);

            //get first frame and info
            BufferedImage firstImg = reader.read(0);
            width = firstImg.getWidth();
            height = firstImg.getHeight();
            cache.addLast(firstImg);

            //get rest of the frames
            maxFrameIndex = reader.getNumImages(true);
            duration = 0;
            for (int i = 1; i < maxFrameIndex; i++) {
                BufferedImage image = reader.read(i);
                cache.addLast(image);

                IIOMetadata metadata = reader.getImageMetadata(i);
                String metaFormatName = metadata.getNativeMetadataFormatName();
                IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metaFormatName);

                IIOMetadataNode graphicsControlExtensionNode = getNode(root, "GraphicControlExtension");
                if (graphicsControlExtensionNode != null) {
                    String delayTime = graphicsControlExtensionNode.getAttribute("delayTime");
                    duration += Integer.parseInt(delayTime) * 10; // delayTime is in 1/100th seconds
                }
            }

            duration /= 1000d;
            frameRate = maxFrameIndex / duration;

            reader.dispose();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private IIOMetadataNode getNode(IIOMetadataNode rootNode, String nodeName) {
        int nNodes = rootNode.getLength();
        for (int i = 0; i < nNodes; i++) {
            if (rootNode.item(i).getNodeName().equalsIgnoreCase(nodeName)) {
                return (IIOMetadataNode) rootNode.item(i);
            }
        }
        return null;
    }

    @Override
    public BufferedImage getCurrentFrame(Integer index) {
        List<BufferedImage> cacheCopy = List.copyOf(cache);
        return cacheCopy.get(index);
    }


}

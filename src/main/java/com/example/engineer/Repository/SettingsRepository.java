package com.example.engineer.Repository;

import com.example.engineer.Model.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettingsRepository extends JpaRepository<UserSettings,Long> {
}

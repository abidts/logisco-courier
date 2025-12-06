package com.logisco.service;

import com.logisco.model.SystemSettings;
import com.logisco.repository.SystemSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class SystemSettingsService {

    @Autowired
    private SystemSettingsRepository settingsRepository;

    public SystemSettings createOrUpdateSetting(SystemSettings settings) {
        Optional<SystemSettings> existing = settingsRepository
                .findBySettingKey(settings.getSettingKey());

        if (existing.isPresent()) {
            SystemSettings existingSetting = existing.get();
            existingSetting.setSettingValue(settings.getSettingValue());
            existingSetting.setEnabled(settings.isEnabled());
            existingSetting.setDescription(settings.getDescription());
            return settingsRepository.save(existingSetting);
        }

        return settingsRepository.save(settings);
    }

    public Optional<SystemSettings> getSetting(String key) {
        return settingsRepository.findBySettingKey(key);
    }

    public List<SystemSettings> getAllSettings() {
        return settingsRepository.findAll();
    }

    public void deleteSetting(Long id) {
        settingsRepository.deleteById(id);
    }

    public boolean isFeatureEnabled(String key) {
        Optional<SystemSettings> setting = settingsRepository.findBySettingKey(key);
        return setting.map(SystemSettings::isEnabled).orElse(false);
    }
}


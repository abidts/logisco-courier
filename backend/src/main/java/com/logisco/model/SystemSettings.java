package com.logisco.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "system_settings")
@Data
public class SystemSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String settingKey;

    private String settingValue;
    private String description;
    private boolean enabled = true;

    @Enumerated(EnumType.STRING)
    private SettingType type;

    public enum SettingType {
        BOOLEAN, STRING, NUMBER, EMAIL
    }
}


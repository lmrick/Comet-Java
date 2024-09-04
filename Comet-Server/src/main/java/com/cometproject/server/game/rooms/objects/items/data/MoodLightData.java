package com.cometproject.server.game.rooms.objects.items.data;

import java.util.List;


public class MoodLightData {
    private boolean enabled;
    private int activePreset;
    private final List<MoodLightPresetData> presets;

    public MoodLightData(boolean enabled, int activePreset, List<MoodLightPresetData> presets) {
        this.enabled = enabled;
        this.activePreset = activePreset;
        this.presets = presets;
    }

    public void updatePreset(int presetIndex, boolean bgOnly, String color, int intensity) {
        if (presets.get(presetIndex) == null) {
            return;
        }
        MoodLightPresetData data = presets.get(presetIndex);

        data.backgroundOnly = bgOnly;
        data.colour = color;
        data.intensity = intensity;
    }

    public List<MoodLightPresetData> getPresets() {
        return presets;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getActivePreset() {
        return activePreset;
    }

    public void setActivePreset(int activePreset) {
        this.activePreset = activePreset;
    }
}

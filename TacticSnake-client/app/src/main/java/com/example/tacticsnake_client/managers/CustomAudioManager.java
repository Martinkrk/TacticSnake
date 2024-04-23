package com.example.tacticsnake_client.managers;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.widget.Button;
import android.widget.SeekBar;
import com.example.tacticsnake_client.R;

import java.util.HashMap;

public class CustomAudioManager {
    private AudioAttributes at;
    private SoundPool sp;
    private AudioManager am;
    private int maxVolume;
    private int currentVolume;
    private int savedVolume;
    private HashMap<String, Integer> sounds;

    public CustomAudioManager(Context context, AudioManager am) {
        this.at = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_GAME)
                .build();

        this.sp = new SoundPool.Builder()
                .setAudioAttributes(at)
                .build();

        this.am = am;
        this.maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        this.currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        this.savedVolume = currentVolume;

        sounds = new HashMap<>();
        sounds.put("click", sp.load(context, R.raw.click, 0));
        sounds.put("ping", sp.load(context, R.raw.ping, 0));
        sounds.put("start", sp.load(context, R.raw.start, 0));
    }

    public void muteSounds() {
        this.savedVolume = this.currentVolume;
        this.am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
    }

    public void unmuteSounds() {
        this.am.setStreamVolume(AudioManager.STREAM_MUSIC, this.savedVolume, 0);
    }

    public SoundPool getSp() {
        return sp;
    }

    public AudioManager getAm() {
        return am;
    }

    public int getMaxVolume() {
        return maxVolume;
    }

    public int getCurrentVolume() {
        return currentVolume;
    }

    public void setCurrentVolume(int currentVolume) {
        this.currentVolume = currentVolume;
    }

    public int getSavedVolume() { return savedVolume; }

    public void setSavedVolume(int savedVolume) {
        this.savedVolume = savedVolume;
    }

    public HashMap<String, Integer> getSounds() { return sounds; }
}

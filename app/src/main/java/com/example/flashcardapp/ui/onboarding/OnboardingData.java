package com.example.flashcardapp.ui.onboarding;

public class OnboardingData {
    public final String emoji, title, subtitle, tip, colorStart, colorMid, colorEnd;

    public OnboardingData(String emoji, String title, String subtitle, String tip,
                          String colorStart, String colorMid, String colorEnd) {
        this.emoji = emoji; this.title = title; this.subtitle = subtitle; this.tip = tip;
        this.colorStart = colorStart; this.colorMid = colorMid; this.colorEnd = colorEnd;
    }
}

/*
 * Copyright 2020 Stefan Schüller <sschueller@techdroid.com>
 *
 * License: GPL-3.0+
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.schueller.instarepost.activities;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;

import net.schueller.instarepost.R;

public class IntroActivity extends AppIntro2 {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Note here that we DO NOT use setContentView();

        // Instead of fragments, you can also use our default slide
        // Just set a title, description, background and image. AppIntro will do the rest.
        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_slide1_title),
                getString(R.string.intro_slide1_description), R.drawable.slide1, Color.parseColor("#121212")));
        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_slide2_title),
                getString(R.string.intro_slide2_description), R.drawable.slide2, Color.parseColor("#121212")));
        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_slide3_title),
                getString(R.string.intro_slide3_description), R.drawable.slide3, Color.parseColor("#121212")));
        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_slide4_title),
                getString(R.string.intro_slide4_description), R.drawable.slide4, Color.parseColor("#121212")));
        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_slide5_title),
                getString(R.string.intro_slide5_description), R.drawable.slide5, Color.parseColor("#121212")));
        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_slide6_title),
                getString(R.string.intro_slide6_description), R.drawable.slide6, Color.parseColor("#121212")));
        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_slide7_title),
                getString(R.string.intro_slide7_description), R.drawable.slide7, Color.parseColor("#121212")));
        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_slide8_title),
                getString(R.string.intro_slide8_description), R.drawable.slide8, Color.parseColor("#121212")));
        // OPTIONAL METHODS
        // Override bar/separator color.
        //setBarColor(Color.parseColor("#3F51B5"));
        //setSeparatorColor(Color.parseColor("#2196F3"));

        // Hide Skip/Done button.
        showSkipButton(true);
        setProgressButtonEnabled(true);

        // Turn vibration on and set intensity.
        // NOTE: you will probably need to ask VIBRATE permission in Manifest.
        setVibrate(false);
        //setVibrateIntensity(30);
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        // Do something when users tap on Skip button.
        finish();
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        // Do something when users tap on Done button.
        finish();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // Do something when the slide changes.
    }
}
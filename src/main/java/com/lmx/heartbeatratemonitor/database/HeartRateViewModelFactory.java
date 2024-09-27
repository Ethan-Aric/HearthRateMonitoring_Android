package com.lmx.heartbeatratemonitor.database;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class HeartRateViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;
    private final HeartRateRepository repository;

    public HeartRateViewModelFactory(Application application, HeartRateRepository repository) {
        this.application = application;
        this.repository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(HeartRateViewModel.class)) {
            return (T) new HeartRateViewModel(application, repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
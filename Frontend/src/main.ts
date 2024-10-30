/// <reference types="@angular/localize" />

import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';
import { importProvidersFrom } from '@angular/core';
import { HttpClientModule } from '@angular/common/http';

bootstrapApplication(AppComponent, appConfig)
  .catch((err) => console.error(err));

    // Bootstrap la aplicación en modo standalone
    bootstrapApplication(AppComponent, {
      providers: [
        importProvidersFrom(HttpClientModule),
        ...appConfig.providers
      ]
    })
    .catch((err) => console.error(err));
/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.tools.intellij.appengine.sdk;

import com.google.cloud.tools.intellij.appengine.sdk.impl.AppEngineSdkUtil;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation of {@link CloudSdkService} backed by {@link PropertiesComponent} for
 * serialization.
 */
public class DefaultCloudSdkService extends CloudSdkService {
  private static final Logger logger = Logger.getInstance(DefaultCloudSdkService.class);

  private PropertiesComponent propertiesComponent;
  private static final String CLOUD_SDK_PROPERTY_KEY = "GCT_CLOUD_SDK_HOME_PATH";

  private Map<String, Set<String>> classesWhiteList;

  public DefaultCloudSdkService() {
    this.propertiesComponent = PropertiesComponent.getInstance();
  }

  @Override
  public String getCloudSdkHomePath() {
    return propertiesComponent.getValue(CLOUD_SDK_PROPERTY_KEY);
  }

  @Override
  public void setCloudSdkHomePath(String cloudSdkHomePath) {
    propertiesComponent.setValue(CLOUD_SDK_PROPERTY_KEY, cloudSdkHomePath);
  }

  @Override
  public boolean isClassInWhiteList(@NotNull String className) {
    if (classesWhiteList == null) {
      if (cachedWhiteList.exists()) {
        try {
          classesWhiteList = AppEngineSdkUtil.loadWhiteList(cachedWhiteList);
        } catch (IOException ioe) {
          logger.error(ioe);
          classesWhiteList = Collections.emptyMap();
        }
      } else {
        classesWhiteList = AppEngineSdkUtil.computeWhiteList();
        if (!classesWhiteList.isEmpty()) {
          AppEngineSdkUtil.saveWhiteList(cachedWhiteList, classesWhiteList);
        }
      }
    }
    if (classesWhiteList.isEmpty()) {
      //don't report errors if white-list wasn't properly loaded
      return true;
    }

    final String packageName = StringUtil.getPackageName(className);
    final String name = StringUtil.getShortName(className);
    final Set<String> classes = classesWhiteList.get(packageName);
    return classes != null && classes.contains(name);
  }

  @Override
  public boolean isMethodInBlacklist(@NotNull String className, @NotNull String methodName) {
    return false;
  }

  @Override
  public String getOrmLibDirectoryPath() {
    return null;
  }

  @NotNull
  @Override
  public List<String> getUserLibraryPaths() {
    return null;
  }

  @NotNull
  @Override
  public VirtualFile[] getOrmLibSources() {
    return new VirtualFile[0];
  }

  @NotNull
  @Override
  public File getApplicationSchemeFile() {
    return null;
  }

  @NotNull
  @Override
  public File getWebSchemeFile() {
    return null;
  }
}

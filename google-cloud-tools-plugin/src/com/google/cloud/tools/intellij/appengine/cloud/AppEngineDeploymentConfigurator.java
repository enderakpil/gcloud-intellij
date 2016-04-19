/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.google.cloud.tools.intellij.appengine.cloud;

import com.google.cloud.tools.intellij.appengine.util.CloudSdkUtil;

import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModulePointer;
import com.intellij.openapi.module.ModulePointerManager;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.packaging.artifacts.Artifact;
import com.intellij.packaging.artifacts.ArtifactManager;
import com.intellij.remoteServer.configuration.RemoteServer;
import com.intellij.remoteServer.configuration.deployment.DeploymentConfigurator;
import com.intellij.remoteServer.configuration.deployment.DeploymentSource;
import com.intellij.remoteServer.configuration.deployment.JavaDeploymentSourceUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.plugins.gradle.util.GradleConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Sets up the configuration elements for an AppEngine Cloud deployment.
 */
class AppEngineDeploymentConfigurator extends
    DeploymentConfigurator<AppEngineDeploymentConfiguration, AppEngineServerConfiguration> {

  private final Project project;

  public AppEngineDeploymentConfigurator(Project project) {
    this.project = project;
  }

  @NotNull
  @Override
  public List<DeploymentSource> getAvailableDeploymentSources() {
    List<DeploymentSource> deploymentSources = new ArrayList<>();

    for (Module module : ModuleManager.getInstance(project).getModules()) {
      if (isMavenBuild(module)) {
        deploymentSources.add(
            new MavenBuildDeploymentSource(
                ModulePointerManager.getInstance(project).create(module), project));
      }
      if (isGradleBuild(module)) {
        deploymentSources.add(
            new GradleBuildDeploymentSource(
                ModulePointerManager.getInstance(project).create(module), project));
      }
    }

    ModulePointer modulePointer =
        ModulePointerManager.getInstance(project).create("userSpecifiedSource");
    deploymentSources.add(new UserSpecifiedPathDeploymentSource(modulePointer));

    deploymentSources.addAll(JavaDeploymentSourceUtil
        .getInstance().createArtifactDeploymentSources(project, getJarsAndWars()));

    return deploymentSources;
  }

  private boolean isMavenBuild(Module module) {
    return MavenProjectsManager.getInstance(project).isMavenizedModule(module)
        && MavenProjectsManager.getInstance(project).findProject(module) != null;
  }

  private boolean isGradleBuild(Module module) {
    return ExternalSystemApiUtil.isExternalSystemAwareModule(GradleConstants.SYSTEM_ID, module);
  }

  private List<Artifact> getJarsAndWars() {
    List<Artifact> jarsAndWars = new ArrayList<Artifact>();
    for (Artifact artifact : ArtifactManager.getInstance(project).getArtifacts()) {
      if (artifact.getArtifactType().getId().equalsIgnoreCase("jar")
          || artifact.getArtifactType().getId().equalsIgnoreCase("war")) {
        jarsAndWars.add(artifact);
      }
    }

    Collections.sort(jarsAndWars, ArtifactManager.ARTIFACT_COMPARATOR);
    return jarsAndWars;
  }

  @NotNull
  @Override
  public AppEngineDeploymentConfiguration createDefaultConfiguration(
      @NotNull DeploymentSource source) {
    return new AppEngineDeploymentConfiguration();
  }

  @Nullable
  @Override
  public SettingsEditor<AppEngineDeploymentConfiguration> createEditor(
      @NotNull DeploymentSource source,
      @NotNull RemoteServer<AppEngineServerConfiguration> server) {
    return new AppEngineDeploymentRunConfigurationEditor(project, source,
        server.getConfiguration(),
        new CloudSdkAppEngineHelper(
            new File(
                CloudSdkUtil.toExecutablePath(server.getConfiguration().getCloudSdkHomePath())),
            server.getConfiguration().getCloudProjectName(),
            server.getConfiguration().getGoogleUserName()));
  }
}

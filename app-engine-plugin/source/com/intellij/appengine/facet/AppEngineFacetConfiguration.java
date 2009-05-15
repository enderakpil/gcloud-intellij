package com.intellij.appengine.facet;

import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.xmlb.annotations.AbstractCollection;
import com.intellij.util.xmlb.annotations.Tag;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * @author nik
 */
public class AppEngineFacetConfiguration implements FacetConfiguration, PersistentStateComponent<AppEngineFacetConfiguration> {
  private String mySdkHomePath = "";
  private boolean myRunEnhancerOnMake = false;
  private List<String> myFilesToEnhance = new ArrayList<String>();

  public FacetEditorTab[] createEditorTabs(FacetEditorContext editorContext, FacetValidatorsManager validatorsManager) {
    return new FacetEditorTab[] {
       new AppEngineFacetEditor(this, editorContext, validatorsManager)
    };
  }

  public void readExternal(Element element) throws InvalidDataException {
  }

  public void writeExternal(Element element) throws WriteExternalException {
  }

  public AppEngineFacetConfiguration getState() {
    return this;
  }

  public void loadState(AppEngineFacetConfiguration state) {
    //todo[nik] remove toSystemIndependentName call later. It is needed only to fix incorrect config files
    mySdkHomePath = FileUtil.toSystemIndependentName(state.getSdkHomePath());
    myRunEnhancerOnMake = state.isRunEnhancerOnMake();
    myFilesToEnhance = state.getFilesToEnhance();
  }

  @Tag("sdk-home-path")
  public String getSdkHomePath() {
    return mySdkHomePath;
  }

  public void setSdkHomePath(String sdkHomePath) {
    mySdkHomePath = sdkHomePath;
  }

  @Tag("run-enhancer-on-make")
  public boolean isRunEnhancerOnMake() {
    return myRunEnhancerOnMake;
  }

  @Tag("files-to-enhance")
  @AbstractCollection(surroundWithTag = false, elementTag = "file", elementValueAttribute = "path")
  public List<String> getFilesToEnhance() {
    return myFilesToEnhance;
  }

  public void setFilesToEnhance(List<String> filesToEnhance) {
    myFilesToEnhance = filesToEnhance;
  }

  public void setRunEnhancerOnMake(boolean runEnhancerOnMake) {
    myRunEnhancerOnMake = runEnhancerOnMake;
  }

}
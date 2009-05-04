package com.intellij.appengine.server.instance;

import com.intellij.debugger.DebuggerManager;
import com.intellij.debugger.engine.DebugProcessAdapter;
import com.intellij.debugger.engine.DebugProcess;
import com.intellij.debugger.engine.DefaultJSPPositionManager;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.javaee.run.configuration.CommonModel;
import com.intellij.javaee.serverInstances.DefaultServerInstance;
import com.intellij.javaee.facet.JavaeeFacetUtil;
import com.intellij.openapi.project.Project;

/**
 * @author nik
 */
public class AppEngineServerInstance extends DefaultServerInstance {
  public AppEngineServerInstance(CommonModel runConfiguration) {
    super(runConfiguration);
  }

  @Override
  public void start(ProcessHandler processHandler) {
    final Project project = getCommonModel().getProject();
    DebuggerManager.getInstance(project).addDebugProcessListener(processHandler, new DebugProcessAdapter() {
      @Override
      public void processAttached(DebugProcess process) {
        process.appendPositionManager(new DefaultJSPPositionManager(process, JavaeeFacetUtil.getInstance().getAllJavaeeFacets(project)) {
          @Override
          protected String getGeneratedClassesPackage() {
            return "org.apache.jsp";
          }
        });
      }
    });
  }
}
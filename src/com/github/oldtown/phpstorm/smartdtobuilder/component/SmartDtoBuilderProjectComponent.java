package com.github.oldtown.phpstorm.smartdtobuilder.component;

import com.github.oldtown.phpstorm.smartdtobuilder.action.CreateDtoBuilder;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class SmartDtoBuilderProjectComponent implements ProjectComponent {
    private Project myProject;
    final private Config config;

    public SmartDtoBuilderProjectComponent(Project project) {
        this();
        myProject = project;

    }
    private SmartDtoBuilderProjectComponent() {

        config = new Config();
    }

    @Override
    public @NotNull String getComponentName() {
        return "OldTownPhpStormPluginSmartDtoBuilder";
    }

    @Override
    public void projectOpened() {
        registerAction();
        //TODO УДалить очистку зарегестрированных шаблонов
        clearTemplate();
    }

    private void clearTemplate() {
        FileTemplateManager fileTemplateManager = FileTemplateManager.getInstance(myProject);
        String[] templates = {
                "PHP Has Method",
                config.getGetterTemplateName(),
                config.getSetterTemplateName(),
                config.getHasTemplateName()
        };
        for (String template: templates) {
            FileTemplate ft = fileTemplateManager.getTemplate(template);
            if (ft != null) {
                fileTemplateManager.removeTemplate(ft);
            }
        }
        fileTemplateManager.saveAllTemplates();

    }

    private void registerAction() {
        ActionManager am = ActionManager.getInstance();
        CreateDtoBuilder action = new CreateDtoBuilder();

        // Регистрируем действие.
        am.registerAction("OldTownSmartDtoBuilder", action);
        // Получаем группу, к которой будет присоединено наше действие.
        DefaultActionGroup windowM = (DefaultActionGroup) am.getAction("PhpGenerateGroup");
        // Добавляем к группе разделитель и действие.
        windowM.addSeparator();
        windowM.add(action);
    }

    @NotNull public Config getConfig() {
        return config;
    }
}

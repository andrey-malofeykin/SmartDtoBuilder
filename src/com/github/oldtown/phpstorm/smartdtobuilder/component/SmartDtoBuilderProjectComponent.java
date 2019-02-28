package com.github.oldtown.phpstorm.smartdtobuilder.component;

import com.github.oldtown.phpstorm.smartdtobuilder.action.CreateDtoBuilder;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.components.ProjectComponent;
import org.jetbrains.annotations.NotNull;

public class SmartDtoBuilderProjectComponent implements ProjectComponent {
    @Override
    public @NotNull String getComponentName() {
        return "OldTownPhpStormPluginSmartDtoBuilder";
    }

    @Override
    public void projectOpened() {
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
}

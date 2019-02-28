package com.github.oldtown.phpstorm.smartdtobuilder.action;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.actions.CodeInsightAction;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.Presentation;
import org.jetbrains.annotations.NotNull;

public class CreateDtoBuilder extends CodeInsightAction {
    private final CreateDtoBuilderHandler myHandler = new CreateDtoBuilderHandler();
    public CreateDtoBuilder() {
        super();
        @NotNull Presentation presentation = getTemplatePresentation();
        presentation.setText("Create DTO");
        presentation.setDescription("Smart creation of DTO");
        presentation.setIcon(AllIcons.Actions.New);
    }

    @Override
    protected @NotNull CodeInsightActionHandler getHandler() {
        return myHandler;
    }


}

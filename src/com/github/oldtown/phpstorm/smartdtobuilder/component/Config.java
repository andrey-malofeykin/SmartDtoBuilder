package com.github.oldtown.phpstorm.smartdtobuilder.component;

public class Config  {
    private String getterTemplateName = "DtoSmartBuilder - PHP Getter Method.php";
    private String setterTemplateName = "DtoSmartBuilder - PHP Setter Method.php";
    private String hasTemplateName = "DtoSmartBuilder - PHP Has Method.php";

    public String getGetterTemplateName() {
        return getterTemplateName;
    }

    public String getSetterTemplateName() {
        return setterTemplateName;
    }

    public String getHasTemplateName() {
        return hasTemplateName;
    }
}

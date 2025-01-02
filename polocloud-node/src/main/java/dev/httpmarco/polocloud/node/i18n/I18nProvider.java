package dev.httpmarco.polocloud.node.i18n;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public class I18nProvider implements I18n {

    private final String resourceBundlePrefix;

    @Override
    public String get(String key) {
        return get(key, Collections.emptyList());
    }

    @Override
    public String get(String key, Object... format) {
        return getDefault(key, format);
    }

    @Override
    public String get(String key, Locale locale, Object... format) {
        var resourceBundle = this.resourceBundle(locale);

        if(!resourceBundle.containsKey(key)) {
            return key;
        }

        if(format.length == 0) {
            return resourceBundle.getString(key);
        }

        return String.format(resourceBundle.getString(key), format);
    }

    @Override
    public String getDefault(String key, Object... format) {
        return get(key, Locale.ENGLISH, format);
    }

    @Override
    public ResourceBundle resourceBundle(Locale locale) {
        try {
            return this.localBundle(locale);
        }catch (MissingResourceException exception) {
            return defaultResourceBundle();
        }
    }

    @Override
    public ResourceBundle defaultResourceBundle() {
        return this.localBundle(Locale.ENGLISH);
    }

    public ResourceBundle localBundle(Locale locale) {
        return ResourceBundle.getBundle(this.resourceBundlePrefix, locale, this.getClass().getClassLoader());
    }
}
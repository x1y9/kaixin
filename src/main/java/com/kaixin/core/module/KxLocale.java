package com.kaixin.core.module;

import com.kaixin.core.app.KxConsts;
import com.kaixin.core.util.PropsKeys;
import com.kaixin.core.util.PropsUtil;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * key应该是点分隔，这样可以直接从properties文件中拷贝
 * Created by liuzhongshu on 2016/9/16.
 */
public class KxLocale {
    private Map<String, ResourceBundle> msgBundles;

    public KxLocale() {
        msgBundles = new HashMap<>();

        String[] allLangs = PropsUtil.getArray(PropsKeys.SYS_ALL_LOCALES);
        for (int i = 0; i < allLangs.length; i++) {
            msgBundles.put(KxConsts.ALL_LANGS[i], ResourceBundle.getBundle("lang/lang", Locale.forLanguageTag(allLangs[i])));
        }
    }

    public String get(String key) {
        return get(key, PropsUtil.get(PropsKeys.SYS_LOCALE), null);
    }

    public String get(String key, Object[] paras) {
        return get(key, PropsUtil.get(PropsKeys.SYS_LOCALE), paras);
    }

    public String get(String key, String locale, Object[] paras) {
        String ret = null;

        try {
            if (locale != null && msgBundles.get(locale) != null)
                ret = msgBundles.get(locale).getString(key);

            if (ret == null)
                ret = msgBundles.get(PropsUtil.get(PropsKeys.SYS_LOCALE)).getString(key);
        }catch(Exception e) {
            //msgBundle.getString可能抛出MissingResourceException
        }

        if (ret == null)
            ret = key.replace("-", " ");

        if (paras != null) {
            ret = String.format(ret, paras);
        }
        return ret;
    }
}

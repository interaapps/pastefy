package de.interaapps.pastefy.helper;

import org.javawebstack.abstractdata.AbstractArray;
import org.javawebstack.abstractdata.AbstractElement;
import org.javawebstack.abstractdata.AbstractNull;

public class AbstractDataHelper {
    public static AbstractElement ifArrayConvertToArray(AbstractElement element) {
        if (element.isArray() || !element.isObject()) return element;

        for (String key : element.object().keys()) {
            if (!key.matches("^[0-9]+$")) return element;
        }
        AbstractArray array = new AbstractArray();
        for (String key : element.object().keys()) {
            int i = Integer.parseInt(key);
            /* if (array.get(i) != AbstractNull.VALUE) {

            } else if (array.get(i).isArray()) {

            }*/

            array.set(i, element.object().get(key));
        }
        return array;
    }
}

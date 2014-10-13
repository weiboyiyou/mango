/*
 * Copyright 2014 mango.jfaster.org
 *
 * The Mango Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.jfaster.mango.util.reflect;

import org.jfaster.mango.exception.UncheckedException;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;

/**
 * @author ash
 */
public class Beans {

    public static void setPropertyValue(Object object, String propertyPath, Object value) {
        try {
            Class<?> rootClass = object.getClass();
            int pos = propertyPath.indexOf('.');
            StringBuffer nestedPath = new StringBuffer(propertyPath.length());
            int t = 0;
            while (pos > -1) {
                if (object == null) {
                    throw new NullPointerException(getErrorMessage(nestedPath.toString(), rootClass));
                }
                String propertyName = propertyPath.substring(0, pos);
                if (t != 0) {
                    nestedPath.append(".");
                }
                nestedPath.append(propertyName);
                GetterInvoker invoker = BeanInfoCache.getGetterInvoker(object.getClass(), propertyName);
                object = invoker.invoke(object);
                propertyPath = propertyPath.substring(pos + 1);
                pos = propertyPath.indexOf('.');
                t++;
            }
            if (object == null) {
                throw new NullPointerException(getErrorMessage(nestedPath.toString(), rootClass));
            }
            SetterInvoker invoker = BeanInfoCache.getSetterInvoker(object.getClass(), propertyPath);
            invoker.invoke(object, value);
        } catch (InvocationTargetException e) {
            throw new UncheckedException(e.getMessage(), e.getCause());
        } catch (IllegalAccessException e) {
            throw new UncheckedException(e.getMessage(), e.getCause());
        }
    }

    public static Object getPropertyValue(@Nullable Object object, String propertyPath, Object useForException) {
        Object value = getNullablePropertyValue(object, propertyPath, useForException);
        if (value == null) {
            throw new NullPointerException(getErrorMessage(propertyPath, useForException));
        }
        return value;
    }

    @Nullable
    public static Object getNullablePropertyValue(@Nullable Object object, String propertyPath, Object useForException) {
        try {
            Object value = object;
            int pos = propertyPath.indexOf('.');
            StringBuffer nestedPath = new StringBuffer(propertyPath.length());
            int t = 0;
            while (pos > -1) {
                if (value == null) {
                    throw new NullPointerException(getErrorMessage(nestedPath.toString(), useForException));
                }
                String propertyName = propertyPath.substring(0, pos);
                if (t != 0) {
                    nestedPath.append(".");
                }
                nestedPath.append(propertyName);
                GetterInvoker invoker = BeanInfoCache.getGetterInvoker(value.getClass(), propertyName);
                value = invoker.invoke(value);
                propertyPath = propertyPath.substring(pos + 1);
                pos = propertyPath.indexOf('.');
                t++;
            }
            if (value == null) {
                throw new NullPointerException(getErrorMessage(nestedPath.toString(), useForException));
            }
            GetterInvoker invoker = BeanInfoCache.getGetterInvoker(value.getClass(), propertyPath);
            value = invoker.invoke(value);
            return value;
        } catch (InvocationTargetException e) {
            throw new UncheckedException(e.getMessage(), e.getCause());
        } catch (IllegalAccessException e) {
            throw new UncheckedException(e.getMessage(), e.getCause());
        }
    }

    private static String getErrorMessage(String nestedPath, Object useForException) {
        if (useForException instanceof String) {
            String parameterName = (String) useForException;
            if (nestedPath.isEmpty()) {
                return "parameter ':" + parameterName + "' is null";
            } else {
                return "property ':" + parameterName + "." + nestedPath + "' is null";
            }
        } else if (useForException instanceof Class) {
            Class<?> clazz = (Class<?>) useForException;
            return "property " + nestedPath + " of " + clazz + " is null, please check return type";
        } else {
            throw new IllegalArgumentException("useForException's type expected Class or String but "
                    + useForException.getClass());
        }
    }

}











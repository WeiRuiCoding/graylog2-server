/**
 * This file is part of Graylog.
 *
 * Graylog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * This file is part of Graylog.
 *
 * Graylog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.graylog2.restroutes.internal;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import org.graylog2.restroutes.PathMethod;

import javax.ws.rs.PathParam;
import java.util.Map;

/**
 * @author Dennis Oelkers <dennis@torch.sh>
 */
public class RouteClassGenerator {
    private final JCodeModel codeModel;
    private final String packagePrefix;

    public RouteClassGenerator(String packagePrefix, JCodeModel codeModel) {
        this.packagePrefix = packagePrefix;
        this.codeModel = codeModel;
    }

    public JDefinedClass generate(RouteClass routeClass) {
        JDefinedClass definedClass;
        try {
            definedClass = codeModel._class(packagePrefix + "." + routeClass.getKlazz().getSimpleName());
        } catch (JClassAlreadyExistsException e) {
            System.out.println("Class " + routeClass.getKlazz().getSimpleName() + " already exists");
            e.printStackTrace();
            return null;
        }

        for (Route route : routeClass.getRoutes()) {
            JMethod method = definedClass.method(JMod.PUBLIC, PathMethod.class, route.getMethod().getName());
            String path = route.getPath();
            for (Map.Entry<PathParam, Class<?>> entry : route.getPathParams().entrySet()) {
                String fieldName = entry.getKey().value();
                method.param(entry.getValue(), fieldName);
                path = path.replaceAll("\\{" + fieldName + "(:?[^}]*)\\}", "\"+com.google.common.net.UrlEscapers.urlPathSegmentEscaper().escape(" + fieldName + ")+\"");
            }
            JBlock block = method.body();
            block.directStatement("return new PathMethod(\"" + route.getHttpMethod() + "\", \"" + path + "\");");
        }

        return definedClass;
    }
}

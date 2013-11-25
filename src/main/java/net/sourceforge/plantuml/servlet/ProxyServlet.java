/* ========================================================================
 * PlantUML : a free UML diagram generator
 * ========================================================================
 *
 * Project Info:  http://plantuml.sourceforge.net
 * 
 * This file is part of PlantUML.
 *
 * PlantUML is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PlantUML distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 */
package net.sourceforge.plantuml.servlet;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import HTTPClient.CookieModule;
import HTTPClient.HTTPConnection;
import HTTPClient.HTTPResponse;
import HTTPClient.ModuleException;
import HTTPClient.ParseException;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;

/* 
 * Proxy servlet of the webapp.
 * This servlet retrieves the diagram source of a web resource (web html page)
 * and renders it.
 */
@SuppressWarnings("serial")
public class ProxyServlet extends HttpServlet {

    private String format;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        final String source = request.getParameter("src");
        final String index = request.getParameter("idx");
        
        // TODO Check if the src URL is valid
        
        // generate the response
        SourceStringReader reader = new SourceStringReader(getSource(source));
        int n = index == null ? 0 : Integer.parseInt(index);
        reader.generateImage(response.getOutputStream(), n, new FileFormatOption(getOutputFormat(), false));
    }

    private String getSource(String uri) throws IOException {
        CookieModule.setCookiePolicyHandler(null);

        final Pattern p = Pattern.compile("http://[^/]+(/?.*)");
        final Matcher m = p.matcher(uri);
        if (m.find() == false) {
            throw new IOException(uri);
        }
        final URL url = new URL(uri);
        final HTTPConnection httpConnection = new HTTPConnection(url);
        try {
            final HTTPResponse resp = httpConnection.Get(m.group(1));
            return resp.getText();
        } catch (ModuleException e) {
            throw new IOException(e.toString());
        } catch (ParseException e) {
            throw new IOException(e.toString());
        }
    }

    private FileFormat getOutputFormat() {
        if (format == null) {
            return FileFormat.PNG;
        }
        if (format.equals("svg")) {
            return FileFormat.SVG;
        }
        if (format.equals("txt")) {
            return FileFormat.UTXT;
        }
        return FileFormat.PNG;
    }

}

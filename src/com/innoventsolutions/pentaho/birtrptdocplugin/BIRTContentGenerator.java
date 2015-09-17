/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
 */
package com.innoventsolutions.pentaho.birtrptdocplugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ListIterator;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.solution.SimpleContentGenerator;
import org.pentaho.platform.util.logging.Logger;

public class BIRTContentGenerator extends SimpleContentGenerator {
	private static final long serialVersionUID = 1L;
	private IPentahoSession session;
	private IUnifiedRepository repository;

	@Override
	public void createContent(final OutputStream out) throws Exception {
	}

	@Override
	public void createContent() throws Exception {
		this.session = PentahoSessionHolder.getSession();
		this.repository = PentahoSystem.get(IUnifiedRepository.class, session);
		final RepositoryFile BIRTfile = (RepositoryFile) parameterProviders
				.get("path").getParameter("file");
		final String ExecBIRTFilePath = "../webapps/birt/" + BIRTfile.getId()
				+ ".rptdocument";
		/*
		 * Get BIRT report design from repository
		 */
		final File ExecBIRTFile = new File(ExecBIRTFilePath);
		if (!ExecBIRTFile.exists()) {
			final FileOutputStream fos = new FileOutputStream(ExecBIRTFilePath);
			try {
				final SimpleRepositoryFileData data = repository
						.getDataForRead(BIRTfile.getId(),
								SimpleRepositoryFileData.class);
				final InputStream inputStream = data.getInputStream();
				final byte[] buffer = new byte[0x1000];
				int bytesRead = inputStream.read(buffer);
				while (bytesRead >= 0) {
					fos.write(buffer, 0, bytesRead);
					bytesRead = inputStream.read(buffer);
				}
			}
			catch (final Exception e) {
				Logger.error(getClass().getName(), e.getMessage());
			}
			finally {
				fos.close();
			}
		}
		/*
		 * Redirect to BIRT Viewer
		 */
		try {
			// Get informations about user context
			final IUserRoleListService service = PentahoSystem
					.get(IUserRoleListService.class);
			String roles = "";
			final ListIterator<String> li = service.getRolesForUser(null,
					session.getName()).listIterator();
			while (li.hasNext()) {
				roles = roles + li.next().toString() + ",";
			}
			// Redirect
			final HttpServletResponse response = (HttpServletResponse) this.parameterProviders
					.get("path").getParameter("httpresponse");
			response.sendRedirect("/birt/frameset?__document="
					+ BIRTfile.getId()
					+ ".rptdocument&__showtitle=false&username="
					+ session.getName() + "&userroles=" + roles
					+ "&reportname=" + BIRTfile.getTitle());
		}
		catch (final Exception e) {
			Logger.error(getClass().getName(), e.getMessage());
		}
	}

	@Override
	public String getMimeType() {
		return "text/html";
	}

	@Override
	public Log getLogger() {
		return LogFactory.getLog(BIRTContentGenerator.class);
	}
}

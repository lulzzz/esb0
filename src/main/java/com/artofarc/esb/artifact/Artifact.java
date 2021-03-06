/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.artofarc.esb.artifact;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Logger;

import com.artofarc.esb.context.GlobalContext;

public abstract class Artifact {

	protected final static Logger logger = Logger.getLogger("ESB");

	private final Directory _parent;
	private final String _name;

	private byte[] _content;
	private long _modificationTime;
	private final HashSet<String> _referenced = new HashSet<>();
	private final HashSet<String> _referencedBy = new HashSet<>();

	private boolean _validated;

	public Artifact(Directory parent, String name) {
		_parent = parent;
		_name = name;
		if (parent != null) {
			parent.getArtifacts().put(name, this);
		}
	}

	public final Directory getParent() {
		return _parent;
	}

	public final String getName() {
		return _name;
	}

	public final String getURI() {
		if (_parent != null) {
			return _parent.getURI() + '/' + _name;
		} else {
			return _name;
		}
	}

	public final static String stripExt(String name) {
		int i = name.lastIndexOf('.');
		return i > 0 ? name.substring(0, i) : name;
	}

	public final Collection<String> getReferenced() {
		return _referenced;
	}

	public final Collection<String> getReferencedBy() {
		return _referencedBy;
	}

	public final boolean isValidated() {
		return _validated;
	}

	public final void setValidated(boolean validated) {
		if (!(_validated = validated)) {
			for (Iterator<String> iterator = _referenced.iterator(); iterator.hasNext();) {
				Artifact artifact = getArtifact(iterator.next());
				artifact.getReferencedBy().remove(getURI());
				iterator.remove();
			}
		}
	}

	public final byte[] getContent() {
		return _content;
	}

	protected final ByteArrayInputStream getContentAsByteArrayInputStream() {
		return new ByteArrayInputStream(_content);
	}

	public long getModificationTime() {
		return _modificationTime;
	}

	public void setModificationTime(long modificationTime) {
		_modificationTime = modificationTime;
	}

	public final void setContent(byte[] _content) {
		this._content = _content;
	}

	protected final Directory getRootDirectory() {
		Directory result = _parent;
		while (result.getParent() != null) {
			result = result.getParent();
		}
		return result;
	}

	protected final <A extends Artifact> A getArtifact(String uri) {
		return FileSystem.getArtifact(getParent(), uri);
	}

	protected final void addReference(Artifact artifact) {
		if (artifact != this) {
			getReferenced().add(artifact.getURI());
			artifact.getReferencedBy().add(getURI());
			logger.fine(getURI() + "->" + artifact);
		}
	}

	protected abstract void validateInternal(GlobalContext globalContext) throws Exception;

	public final void validate(GlobalContext globalContext) throws ValidationException {
		if (!isValidated()) {
			try {
				validateInternal(globalContext);
			} catch (ValidationException e) {
				throw e;
			} catch (Exception e) {
				throw new ValidationException(this, e);
			}
			setValidated(true);
			logger.info("Validated: " + getURI());
		}
	}

	protected final void validateReferenced(GlobalContext globalContext) throws ValidationException {
		for (String artifactUri : getReferenced()) {
			Artifact artifact = getArtifact(artifactUri);
			artifact.validate(globalContext);
		}
	}

	@Override
	public String toString() {
		return getURI();
	}

	protected <A extends Artifact> A initClone(A clone) {
		if (isValidated()) {
			clone.setValidated(true);
			clone.getReferenced().addAll(getReferenced());
			clone.getReferencedBy().addAll(getReferencedBy());
		}
		clone.setModificationTime(getModificationTime());
		clone.setContent(getContent());
		return clone;
	}

	public abstract Artifact clone(Directory parent);

}

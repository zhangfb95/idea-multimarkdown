/*
 * Copyright (c) 2015-2015 Vladimir Schneider <vladimir.schneider@gmail.com>
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.vladsch.idea.multimarkdown.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.vladsch.idea.multimarkdown.language.MultiMarkdownReference;
import com.vladsch.idea.multimarkdown.psi.MultiMarkdownNamedElement;
import com.vladsch.idea.multimarkdown.psi.MultiMarkdownVisitor;
import com.vladsch.idea.multimarkdown.psi.MultiMarkdownWikiPageRef;
import com.vladsch.idea.multimarkdown.util.FilePathInfo;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class MultiMarkdownWikiPageRefImpl extends MultiMarkdownNamedElementImpl implements MultiMarkdownWikiPageRef {
    private static final Logger logger = Logger.getLogger(MultiMarkdownWikiPageRefImpl.class);
    private final MultiMarkdownReference reference;

    public MultiMarkdownWikiPageRefImpl(ASTNode node) {
        super(node);
        reference = new MultiMarkdownReference(this);
    }

    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof MultiMarkdownVisitor) ((MultiMarkdownVisitor) visitor).visitNamedElement(this);
        else super.accept(visitor);
    }

    @Override
    public String getDisplayName() {
        return getFileName();
    }

    @Override
    public String getName() {
        return getText();
    }

    @Override
    public String getFileName() {
        return FilePathInfo.wikiRefAsFileNameWithExt(getName());
    }

    public PsiElement setName(@NotNull String newName) {
        return setName(newName, false);
    }

    public PsiElement setName(String newName, boolean fileMoved) {
        String oldName = getName();
        reference.invalidateResolveResults();
        MultiMarkdownNamedElement element = MultiMarkdownPsiImplUtil.setName(this, newName, fileMoved);

        //logger.info("setName on " + this.toString() + " from " + oldName + " to " + element.getName());
        return element;
    }

    public PsiElement getNameIdentifier() {
        return this; //MultiMarkdownPsiImplUtil.getNameIdentifier(this);
    }

    public ItemPresentation getPresentation() {
        return MultiMarkdownPsiImplUtil.getPresentation(this);
    }

    /**
     * Returns the reference from this PSI element to another PSI element (or elements), if one exists.
     * If the element has multiple associated references (see {@link #getReferences()}
     * for an example), returns the first associated reference.
     *
     * @return the reference instance, or null if the PSI element does not have any
     * associated references.
     * @see com.intellij.psi.search.searches.ReferencesSearch
     */
    @org.jetbrains.annotations.Nullable
    @Override
    public PsiReference getReference() {
        reference.refreshName();
        return reference;
    }

    @Override
    public String toString() {
        return "WIKI_LINK_REF '" + getName() + "' " + super.hashCode();
    }

    ///**
    // * Returns all references from this PSI element to other PSI elements. An element can
    // * have multiple references when, for example, the element is a string literal containing
    // * multiple sub-strings which are valid full-qualified class names. If an element
    // * contains only one text fragment which acts as a reference but the reference has
    // * multiple possible targets, {@link PsiPolyVariantReference} should be used instead
    // * of returning multiple references.
    // * <p/>
    // * Actually, it's preferable to call {@link com.intellij.psi.PsiReferenceService#getReferences} instead
    // * as it allows adding references by plugins when the element implements {@link com.intellij.psi.ContributedReferenceHost}.
    // *
    // * @return the array of references, or an empty array if the element has no associated
    // * references.
    // *
    // * @see com.intellij.psi.PsiReferenceService#getReferences
    // * @see com.intellij.psi.search.searches.ReferencesSearch
    // */
    //@Override
    //@NotNull
    //public PsiReference[] getReferences() {
    //    return SharedPsiElementImplUtil.getReferences(this);
    //}
}

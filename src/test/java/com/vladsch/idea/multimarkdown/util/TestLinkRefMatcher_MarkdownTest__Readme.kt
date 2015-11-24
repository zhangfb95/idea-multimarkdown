/*
 * Copyright (c) 2015-2015 Vladimir Schneider <vladimir.schneider@gmail.com>, all rights reserved.
 *
 * This code is private property of the copyright holder and cannot be used without
 * having obtained a license or prior written permission of the of the copyright holder.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package com.vladsch.idea.multimarkdown.util

import com.vladsch.idea.multimarkdown.TestUtils.*
import com.vladsch.idea.multimarkdown.printData
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.*

@RunWith(value = Parameterized::class)
class TestLinkRefMatcher_MarkdownTest__Readme constructor(val fullPath: String
                                                             , val linkRefType: (containingFile: FileRef, linkRef: String, anchor: String?) -> LinkRef
                                                             , val linkText: String
                                                             , val linkAddress: String
                                                             , val linkAnchor: String?
                                                             , val linkTitle: String?
                                                             , resolvesLocalRel: String?
                                                             , resolvesExternalRel: String?
                                                             , val linkAddressText: String?
                                                             , val remoteAddressText: String?
                                                             , multiResolvePartial: Array<String>
) {
    val resolvesLocal: String?
    val resolvesExternal: String?
    val filePathInfo = FileRef(fullPath)
    val resolver = GitHubLinkResolver(MarkdownTestData, filePathInfo)
    val linkRef = LinkRef.parseLinkRef(filePathInfo, linkAddress + linkAnchor.startWith('#'), linkRefType)
    val linkRefNoExt = LinkRef.parseLinkRef(filePathInfo, linkRef.filePathNoExt + linkAnchor.startWith('#'), linkRefType)
    val fileList = ArrayList<FileRef>(MarkdownTestData.filePaths.size)
    val multiResolve: Array<String>
    val localLinkRef = resolvesLocalRel
    val externalLinkRef = resolvesExternalRel
    val skipTest = linkRef is UrlLinkRef// || (linkRef !is ImageLinkRef && linkRef.hasExt && !linkRef.isMarkdownExt)

    init {
        val fullPathInfo = PathInfo(fullPath)
        val filePathInfo = PathInfo(fullPathInfo.path)
        resolvesLocal = if (resolvesLocalRel == null) null else filePathInfo.append(resolvesLocalRel.splitToSequence("/")).filePath
        resolvesExternal = if (resolvesExternalRel == null) null else if (resolvesExternalRel.startsWith("http://", "https://")) resolvesExternalRel else filePathInfo.append(resolvesExternalRel.splitToSequence("/")).filePath

        var multiResolveAbs = ArrayList<String>()

        if (multiResolvePartial.size == 0 && resolvesLocal != null) multiResolveAbs.add(resolvesLocal)

        for (path in multiResolvePartial) {
            multiResolveAbs.add(filePathInfo.append(path.splitToSequence("/")).filePath)
        }

        multiResolve = multiResolveAbs.toArray(Array(0, { "" }))

        for (path in MarkdownTestData.filePaths) {
            fileList.add(FileRef(path))
        }
    }


    @Test fun test_ResolveLocal() {
        if (skipTest) return
        val localRef = resolver.resolve(linkRef, LinkResolver.ONLY_LOCAL, fileList)
        assertEqualsMessage("Local does not match", resolvesLocal, localRef?.filePath)
    }

    @Test fun test_ResolveExternal() {
        if (skipTest) return
        val localRef = resolver.resolve(linkRef, LinkResolver.ONLY_REMOTE, fileList)
        assertEqualsMessage("External does not match", resolvesExternal, localRef?.filePath)
    }

    @Test fun test_LocalLinkAddress() {
        if (skipTest) return
        val localRef = resolver.resolve(linkRef, LinkResolver.ONLY_LOCAL, fileList) as? FileRef
        val localRefAddress = if (localRef != null) resolver.linkAddress(linkRef, localRef, (linkRef.hasExt || (linkRef.hasAnchor && linkAnchor?.contains('.') ?: false)), null) else null
        assertEqualsMessage("Local link address does not match", this.linkAddressText, localRefAddress)
    }

    @Test fun test_RemoteLinkAddress() {
        if (skipTest) return
        val localRef = resolver.resolve(linkRef, LinkResolver.ONLY_LOCAL, fileList) as? FileRef
        val remoteRef = resolver.resolve(linkRef, LinkResolver.ONLY_REMOTE, fileList) as? PathInfo
        val remoteRefAddress = if (localRef == null && remoteRef != null) resolver.linkAddress(linkRef, remoteRef, linkRef !is WikiLinkRef && (linkRef.hasExt || (linkRef.hasAnchor && linkAnchor?.contains('.') ?: false)), null) else null
        assertEqualsMessage("Remote based link address does not match", this.remoteAddressText, remoteRefAddress)
    }

    @Test fun test_MultiResolve() {
        if (skipTest) return
//        val localRefs = resolver.multiResolve(if (linkRef is WikiLinkRef) linkRef else linkRefNoExt, LinkResolver.ONLY_LOCAL or LinkResolver.LOOSE_MATCH, fileList)
        val localRefs = resolver.multiResolve(linkRef, LinkResolver.ONLY_LOCAL or LinkResolver.LOOSE_MATCH, fileList)
        val actuals = Array<String>(localRefs.size, { "" })
        for (i in localRefs.indices) {
            actuals[i] = localRefs[i].filePath
        }
        compareOrderedLists("MultiResolve does not match", multiResolve, actuals)
    }

    companion object {
        @Parameterized.Parameters(name = "{index}: filePath = {0}, linkRef = {3}, linkAnchor = {4}")
        @JvmStatic
        fun data(): Collection<Array<Any?>> {
            return MarkdownTest__Readme_md.data()
        }
    }
}


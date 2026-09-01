package be.dalfarra.avifviewer.editor

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class AvifEditorTest : BasePlatformTestCase() {

    fun testAvifFileEditorProviderAcceptsAvifExtension() {
        val provider = AvifFileEditorProvider()
        
        // Create a virtual file with .avif extension in the mock test environment
        val avifFile = myFixture.createFile("test_image.avif", "dummy content")
        val txtFile = myFixture.createFile("test_file.txt", "dummy content")

        // Assert that the provider targets .avif but ignores other file types
        assertTrue(provider.accept(project, avifFile))
        assertFalse(provider.accept(project, txtFile))
    }

    fun testEditorTypeIdAndPolicy() {
        val provider = AvifFileEditorProvider()
        assertEquals("AvifViewerEditor", provider.getEditorTypeId())
    }
}
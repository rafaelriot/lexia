package com.lexia.legalassistant

import com.lexia.legalassistant.ui.screens.chat.ChatViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatViewModelTest {

    @Test
    fun testInitialState() {
        val vm = ChatViewModel()
        assertTrue(vm.messages.value.isEmpty())
        assertFalse(vm.isGeneratingResponse.value)
    }

    @Test
    fun testAppendUserTextMessage() {
        val vm = ChatViewModel()
        val msg = vm.appendUserMessage(
            query = "¿Qué consagra el artículo 14 constitucional?",
            senderName = "Lic. Rafael",
            isVoice = false
        )

        assertEquals(1, vm.messages.value.size)
        assertEquals("¿Qué consagra el artículo 14 constitucional?", vm.messages.value[0].text)
        assertEquals("Lic. Rafael", vm.messages.value[0].senderName)
        assertTrue(vm.messages.value[0].isUser)
        assertFalse(vm.messages.value[0].isVoiceRecording)
        assertNotNull(msg.id)
    }

    @Test
    fun testAppendUserVoiceMessage() {
        val vm = ChatViewModel()
        val msg = vm.appendUserMessage(
            query = "Consulta sobre flagrancia en artículo 16",
            senderName = "Lic. Rafael",
            isVoice = true,
            voiceFile = null,
            voiceDuration = 5
        )

        assertEquals(1, vm.messages.value.size)
        assertTrue(vm.messages.value[0].isVoiceRecording)
        assertEquals(5, vm.messages.value[0].voiceDurationSeconds)
        assertEquals("Consulta sobre flagrancia en artículo 16", vm.messages.value[0].transcribedText)
    }

    @Test
    fun testClearMessages() {
        val vm = ChatViewModel()
        vm.appendUserMessage("Prueba", "Usuario")
        assertEquals(1, vm.messages.value.size)
        vm.clearMessages()
        assertTrue(vm.messages.value.isEmpty())
    }
}

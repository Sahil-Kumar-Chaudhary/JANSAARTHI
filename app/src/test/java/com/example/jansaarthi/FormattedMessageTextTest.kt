package com.example.jansaarthi

import com.example.jansaarthi.ui.screens.rag.*
import org.junit.Assert.*
import org.junit.Test

class FormattedMessageTextTest {

    @Test
    fun testParseInlineMarkdownBoldAndItalic() {
        val input = "Here is **bold text** and *italic text* and `code sample`."
        val annotated = parseInlineMarkdown(input)

        assertEquals("Here is bold text and italic text and code sample.", annotated.text)
        assertTrue("Expected span styles for bold, italic, code", annotated.spanStyles.isNotEmpty())
    }

    @Test
    fun testCleanMarkdownArtifactsStripsPipesAndAsterisks() {
        val dirty = " | Some cell text | "
        val cleaned = cleanMarkdownArtifacts(dirty)
        assertEquals("Some cell text", cleaned)
    }

    @Test
    fun testParseMessageBlocksHeadings() {
        val markdown = """
            # Primary Heading
            ## Secondary Heading
            ### Tertiary Heading
            **Standalone Bold Heading**
        """.trimIndent()

        val blocks = parseMessageBlocks(markdown)
        assertEquals(4, blocks.size)
        assertTrue(blocks[0] is MessageBlock.Heading && (blocks[0] as MessageBlock.Heading).level == 1)
        assertTrue(blocks[1] is MessageBlock.Heading && (blocks[1] as MessageBlock.Heading).level == 2)
        assertTrue(blocks[2] is MessageBlock.Heading && (blocks[2] as MessageBlock.Heading).level == 3)
        assertTrue(blocks[3] is MessageBlock.Heading && (blocks[3] as MessageBlock.Heading).text == "Standalone Bold Heading")
    }

    @Test
    fun testParseMessageBlocksBulletsAndNumbered() {
        val markdown = """
            - Bullet point one
            * Bullet point two
            • Bullet point three
            1. First step
            2. Second step
        """.trimIndent()

        val blocks = parseMessageBlocks(markdown)
        assertEquals(5, blocks.size)
        assertTrue(blocks[0] is MessageBlock.BulletItem && (blocks[0] as MessageBlock.BulletItem).text == "Bullet point one")
        assertTrue(blocks[1] is MessageBlock.BulletItem && (blocks[1] as MessageBlock.BulletItem).text == "Bullet point two")
        assertTrue(blocks[2] is MessageBlock.BulletItem && (blocks[2] as MessageBlock.BulletItem).text == "Bullet point three")
        assertTrue(blocks[3] is MessageBlock.NumberedItem && (blocks[3] as MessageBlock.NumberedItem).number == "1")
        assertTrue(blocks[4] is MessageBlock.NumberedItem && (blocks[4] as MessageBlock.NumberedItem).number == "2")
    }

    @Test
    fun testParseMarkdownTableConvertsToMobileFriendlyCards() {
        val table = """
            | Scheme | Scheme ID | Launch Date | Target Group |
            |---|---|---|---|
            | Stand Up India | SCH003 | 2016-04-05 | Women entrepreneurs |
            | PM Mudra Yojana | SCH002 | 2015-04-08 | Micro enterprise |
        """.trimIndent()

        val blocks = parseMessageBlocks(table)
        // Two data rows should convert to two TableCards
        assertEquals(2, blocks.size)

        val card1 = blocks[0] as MessageBlock.TableCard
        assertEquals("Stand Up India", card1.title)
        assertEquals(3, card1.entries.size)
        assertEquals("Scheme ID", card1.entries[0].first)
        assertEquals("SCH003", card1.entries[0].second)
        assertEquals("Launch Date", card1.entries[1].first)
        assertEquals("2016-04-05", card1.entries[1].second)

        val card2 = blocks[1] as MessageBlock.TableCard
        assertEquals("PM Mudra Yojana", card2.title)
        assertEquals("Scheme ID", card2.entries[0].first)
        assertEquals("SCH002", card2.entries[0].second)
    }

    @Test
    fun testParseRealisticAiResponseWithoutRawMarkdownLeakage() {
        val realisticResponse = """
            **Financial Grants for Women Entrepreneurs**
            
            Based on verified government records:
            - Stand Up India Scheme (SCH003): Provides credit facilities for women and SC/ST founders.
            - Note: The matching schemes provide repayable loans, not non-repayable grants.
            
            Specific monetary caps and active application status must be verified on myScheme.gov.in.
        """.trimIndent()

        val blocks = parseMessageBlocks(realisticResponse)
        assertTrue("Should have multiple structured blocks", blocks.size >= 4)
        assertTrue("First block should be a heading", blocks[0] is MessageBlock.Heading)
        assertTrue("Second block should be a paragraph", blocks[1] is MessageBlock.Paragraph)
        assertTrue("Third block should be bullet item", blocks[2] is MessageBlock.BulletItem)
        assertTrue("Fourth block should be bullet item", blocks[3] is MessageBlock.BulletItem)
    }
}

package vazkii.tukmc;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glScalef;
import static vazkii.tukmc.TukMCReference.BOX_INNER_COLOR;
import static vazkii.tukmc.TukMCReference.BOX_OUTLINE_COLOR;

import java.awt.Color;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javaQuery.j2ee.tinyURL;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import updatemanager.common.UpdateManager;
import vazkii.codebase.common.ColorCode;
import vazkii.codebase.common.FormattingCode;
import net.minecraft.client.Minecraft;

import net.minecraft.src.ChatClickData;
import net.minecraft.src.MathHelper;
import net.minecraft.src.Tessellator;

import cpw.mods.fml.relauncher.ReflectionHelper;

public class GuiChat extends net.minecraft.src.GuiChat {

	public static final Pattern pattern = Pattern.compile("^(?:(https?)://)?([-\\w_\\.]{2,}\\.[a-z]{2,3})(/\\S*)?$");
	String username;
	String tooltip = "";
	public static final String CHARS = "GSCLPU";

	@Override
	public void initGui() {
		super.initGui();
		username = FormattingCode.BOLD + "<" + mc.thePlayer.username + "> ";
		inputField = new SpellcheckingTextbox(fontRenderer, fontRenderer.getStringWidth(username) + 17 * 2, (height - 98) * 2, 360, 4);
		inputField.setMaxStringLength(100);
		inputField.setEnableBackgroundDrawing(false);
		inputField.setFocused(true);
		inputField.setCanLoseFocus(false);
	}

	@Override
	public void drawScreen(int par1, int par2, float par3) {
		drawDoubleOutlinedBox(15, height - 100, 224, 8, BOX_INNER_COLOR, BOX_OUTLINE_COLOR);
		GL11.glPushMatrix();
		GL11.glScalef(0.5F, 0.5F, 0.5F);
		fontRenderer.drawString(username, 17 * 2, (height - 98) * 2, 0xFFFFFF);
		fontRenderer.drawString(ColorCode.GREY + "Tip: Shift clicking while scrolling will scroll 7x faster!", 175, height * 2 - 211, 0xFFFFFF);
		inputField.drawTextBox();
		GL11.glPopMatrix();
		for (int i = 0; i < CHARS.length(); i++) {
			drawDoubleOutlinedBox(15 + i * 12, height - 112, 9, 10, BOX_INNER_COLOR, BOX_OUTLINE_COLOR);
			fontRenderer.drawStringWithShadow("" + CHARS.charAt(i), 17 + i * 12, height - 111, 0xFFFFFF);
		}
		if (tooltip != "") {
			String[] tokens = tooltip.split(";");
			int length = 12;
			for (String s : tokens)
				length = Math.max(length, fontRenderer.getStringWidth(s));
					drawDoubleOutlinedBox(15, height - 114 - tokens.length * 12, length + 6, tokens.length * 12, BOX_INNER_COLOR, BOX_OUTLINE_COLOR);
					int i = 0;
					for (String s : tokens) {
						fontRenderer.drawStringWithShadow(s, 18, height - 112 - tokens.length * 12 + i * 12, 0xFFFFFF);
						++i;
					}
		}
		if (!MathHelper.stringNullOrLengthZero(mod_TukMC.pinnedMsg)) {
			String pin = "Pinned:";
			drawDoubleOutlinedBox(15, height - 211, fontRenderer.getStringWidth(pin) + 6, 14, BOX_INNER_COLOR, BOX_OUTLINE_COLOR);
			fontRenderer.drawStringWithShadow(pin, 18, height - 210, 0xFFFFFF);
			drawDoubleOutlinedBox(15, height - 200, fontRenderer.getStringWidth(mod_TukMC.pinnedMsg) + 6, 14, BOX_INNER_COLOR, BOX_OUTLINE_COLOR);
			fontRenderer.drawStringWithShadow(mod_TukMC.pinnedMsg, 18, height - 198, 0xFFFFFF);
		}
	}

	@Override
	protected void keyTyped(char par1, int par2) {
		if (par2 == 28 && mod_TukMC.closeOnFinish) mod_TukMC.shouldReopenChat = true;
		super.keyTyped(par1, par2);
	}

	@Override
	public void handleMouseInput() {
		int var1 = Mouse.getEventDWheel();

		int x = Mouse.getX();
		int y = Mouse.getY();
		int box = (x - 9) / ((168 - 28) / CHARS.length()) - 1;
		if (y >= height - 150 && y <= height - 130 && x >= 28 && x <= 168) switch (box) {
			case 0:
				tooltip = "Converts the text in the chat field into a;Let me Google That For You link.";
				break;
			case 1:
				tooltip = "Shortens the link using tinyurl. " + ColorCode.RED + "(May take;" + ColorCode.RED + "a while)";
				break;
			case 2:
				tooltip = (mod_TukMC.spellcheckerEnabled ? "Disables" : "Enables") + " the Spellchecker";
				break;
			case 3:
				tooltip = (mod_TukMC.closeOnFinish ? "Unlocks" : "Locks") + " the Chat GUI (" + (mod_TukMC.closeOnFinish ? "doesn't exit" : "exits") + " after;saying something)";
				break;
			case 4:
				tooltip = "Pins the text in the chat field to the;screen.";
				break;
			case 5:
				tooltip = "Unpins the text pinned to the screen.";
		}
		else tooltip = "";

		int relativeWidth = Mouse.getEventX() * width / mc.displayWidth;
		int relativeHeight = height - Mouse.getEventY() * height / mc.displayHeight - 1;

		if (Mouse.getEventButtonState()) {
			int button = Mouse.getEventButton();
			Minecraft.getSystemTime();
			mouseClicked(relativeWidth, relativeHeight, button);
		} else if (Mouse.getEventButton() != -1) mouseMovedOrUp(relativeWidth, relativeHeight, Mouse.getEventButton());

		if (var1 != 0) {
			if (var1 > 1) var1 = 1;

			if (var1 < -1) var1 = -1;

			if (isShiftKeyDown()) var1 *= 7;

			mc.ingameGUI.getChatGUI().scroll(var1);
		}
	}

	@Override
	protected void mouseClicked(int par1, int par2, int par3) {
		int x = Mouse.getX();
		int y = Mouse.getY();
		int box = (x - 9) / ((168 - 28) / CHARS.length()) - 1;
		if (y >= height - 150 && y <= height - 130 && x >= 28 && x <= 168) switch (box) {
			case 0: {
				URI uri = getURI();
				if (uri == null) {
					String text = inputField.getText();
					if (MathHelper.stringNullOrLengthZero(text)) break;

					String s = "http://lmgtfy.com/?q=" + text.replaceAll(" ", "+");
					inputField.setText(s);
				}
				break;
			}
			case 1: {
				URI uri = getURI();
				if (UpdateManager.online && uri != null) {
					String text = inputField.getText();
					if (text.contains("tinyurl.com/")) break;
					tinyURL url = new tinyURL();
					inputField.setText(url.getTinyURL(text).replaceAll("http://preview.", ""));
				}
				break;
			}
			case 2: {
				mod_TukMC.setSpellcheckerEnabled(!mod_TukMC.spellcheckerEnabled);
				break;
			}
			case 3: {
				mod_TukMC.setCloseOnFinish(!mod_TukMC.closeOnFinish);
				break;
			}
			case 4: {
				String text = inputField.getText();
				if (!MathHelper.stringNullOrLengthZero(text)) {
					mod_TukMC.setPinnedMsg(text);
					inputField.setText("");
				}
				break;
			}
			case 5: {
				inputField.setText(mod_TukMC.pinnedMsg);
				mod_TukMC.setPinnedMsg("");
				break;
			}
		}
		if (par3 == 0 && mc.gameSettings.chatLinks) {
			ChatClickData var4 = mc.ingameGUI.getChatGUI().func_73766_a(Mouse.getX() * 2, Mouse.getY() * 2);

			if (var4 != null) {
				URI var5 = var4.getURI();

				if (var5 != null) {
					if (mc.gameSettings.chatLinksPrompt) {
						ReflectionHelper.setPrivateValue(net.minecraft.src.GuiChat.class, this, var5, 6);
						mc.displayGuiScreen(new GuiChatConfirmLink(this, this, var4.getClickedUrl(), 0, var4));
					}

					return;
				}
			}
		}

		inputField.mouseClicked(par1 * 2, par2 * 2, par3);
	}

	public URI getURI() {
		String var1 = inputField.getText();

		if (var1 == null) return null;
		else {
			Matcher var2 = pattern.matcher(var1);

			if (var2.matches()) try {
				String var3 = var2.group(0);

				if (var2.group(1) == null) var3 = "http://" + var3;

				return new URI(var3);
			} catch (URISyntaxException var4) {
			}

			return null;
		}
	}

	public void drawDoubleOutlinedBox(int x, int y, int width, int height, int color, int outlineColor) {
		glPushMatrix();
		glScalef(0.5F, 0.5F, 0.5F);
		drawSolidRect(x * 2 - 2, y * 2 - 2, (x + width) * 2 + 2, (y + height) * 2 + 2, color);
		drawSolidRect(x * 2 - 1, y * 2 - 1, (x + width) * 2 + 1, (y + height) * 2 + 1, outlineColor);
		drawSolidRect(x * 2, y * 2, (x + width) * 2, (y + height) * 2, color);
		glPopMatrix();
	}

	public void drawSolidRect(int vertex1, int vertex2, int vertex3, int vertex4, int color) {
		glPushMatrix();
		Color color1 = new Color(color);
		Tessellator tess = Tessellator.instance;
		glDisable(GL_TEXTURE_2D);
		tess.startDrawingQuads();
		tess.setColorOpaque(color1.getRed(), color1.getGreen(), color1.getBlue());
		tess.addVertex(vertex1, vertex4, zLevel);
		tess.addVertex(vertex3, vertex4, zLevel);
		tess.addVertex(vertex3, vertex2, zLevel);
		tess.addVertex(vertex1, vertex2, zLevel);
		tess.draw();
		glEnable(GL_TEXTURE_2D);
		glPopMatrix();
	}

}
package com.lilithsthrone.rendering;

/**
 * The title/description of a simple informational tooltip, registered by {@code core} dialogue
 * code into {@code Game.informationTooltips} against the id of the element it belongs to.
 *
 * A plain data holder: {@code core} declares *what* the tooltip says, and each front-end decides
 * how to present it (desktop wraps these in its DOM {@code EventListener}-based tooltip;
 * an HTML/HTTP client can render them as title attributes or its own hover handling). Previously
 * {@code core} constructed desktop's {@code TooltipInformationEventListener} directly, which is
 * why this type exists.
 */
public class InformationTooltip {

	private String title;
	private String description;
	private int descriptionHeightOverride;

	public InformationTooltip setInformation(String title, String description) {
		this.title = title;
		this.description = description;
		return this;
	}

	public InformationTooltip setInformation(String title, String description, int descriptionHeightOverride) {
		setInformation(title, description);
		this.descriptionHeightOverride = descriptionHeightOverride;
		return this;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public int getDescriptionHeightOverride() {
		return descriptionHeightOverride;
	}
}

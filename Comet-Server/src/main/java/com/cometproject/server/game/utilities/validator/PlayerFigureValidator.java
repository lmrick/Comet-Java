package com.cometproject.server.game.utilities.validator;

import com.cometproject.api.config.CometSettings;
import com.cometproject.server.boot.Comet;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

public class PlayerFigureValidator {
	
	private static Map<Integer, Map<Integer, PlayerFigureColor>> palettes;
	private static Map<String, PlayerFigureSetType> setTypes;
	private static Map<String, Map<Integer, List<String>>> mandatorySetTypes;
	
	public static void loadFigureData() {
		try {
			final File figureDataFile = new File("config/figuredata.xml");
			final Document furnidataDocument;
			
			if (!figureDataFile.exists()) {
				furnidataDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(Comet.class.getResourceAsStream("/config/figuredata.xml"));
				FileUtils.copyURLToFile(Comet.class.getResource("/config/figuredata.xml"), figureDataFile);
			} else {
				furnidataDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(figureDataFile);
			}
			
			final Element furnidata = (Element) furnidataDocument.getElementsByTagName("figuredata").item(0);
			
			PlayerFigureValidator.palettes = new ConcurrentHashMap<>();
			PlayerFigureValidator.setTypes = new ConcurrentHashMap<>();
			
			PlayerFigureValidator.mandatorySetTypes = new ConcurrentHashMap<>();
			PlayerFigureValidator.mandatorySetTypes.put("m", new ConcurrentHashMap<>());
			PlayerFigureValidator.mandatorySetTypes.get("m").put(0, new ArrayList<>());
			PlayerFigureValidator.mandatorySetTypes.get("m").put(1, new ArrayList<>());
			PlayerFigureValidator.mandatorySetTypes.get("m").put(2, new ArrayList<>());
			PlayerFigureValidator.mandatorySetTypes.put("f", new ConcurrentHashMap<>());
			PlayerFigureValidator.mandatorySetTypes.get("f").put(0, new ArrayList<>());
			PlayerFigureValidator.mandatorySetTypes.get("f").put(1, new ArrayList<>());
			PlayerFigureValidator.mandatorySetTypes.get("f").put(2, new ArrayList<>());
			
			final NodeList palettes = furnidata.getElementsByTagName("palette");
			
			IntStream.range(0, palettes.getLength()).mapToObj(palettes::item).filter(paletteNode -> paletteNode.getNodeType() == Node.ELEMENT_NODE).map(Element.class::cast).forEachOrdered(paletteElement -> {
				final int paletteId = Integer.parseInt(paletteElement.getAttribute("id"));
				PlayerFigureValidator.palettes.put(paletteId, new ConcurrentHashMap<>());
				final NodeList colors = paletteElement.getElementsByTagName("color");
				IntStream.range(0, colors.getLength()).mapToObj(colors::item).filter(colorNode -> colorNode.getNodeType() == Node.ELEMENT_NODE).map(Element.class::cast).forEachOrdered(colorElement -> {
					final int colorId = Integer.parseInt(colorElement.getAttribute("id"));
					PlayerFigureValidator.palettes.get(paletteId).put(colorId, new PlayerFigureColor(Integer.parseInt(colorElement.getAttribute("club")), Integer.parseInt(colorElement.getAttribute("selectable")) == 1));
				});
			});
			
			final NodeList setTypes = furnidata.getElementsByTagName("settype");
			
			for (int setTypeIndex = 0; setTypeIndex < setTypes.getLength(); ++setTypeIndex) {
				final Node setTypeNode = setTypes.item(setTypeIndex);
				
				if (setTypeNode.getNodeType() == Node.ELEMENT_NODE) {
					final Element setTypeElement = (Element) setTypeNode;
					final String typeName = setTypeElement.getAttribute("type").toLowerCase();
					
					if (Integer.parseInt(setTypeElement.getAttribute("mand_m_0")) > 0) {
						PlayerFigureValidator.mandatorySetTypes.get("m").get(0).add(typeName);
					}
					
					if (Integer.parseInt(setTypeElement.getAttribute("mand_f_0")) > 0) {
						PlayerFigureValidator.mandatorySetTypes.get("m").get(0).add(typeName);
					}
					
					if (Integer.parseInt(setTypeElement.getAttribute("mand_m_1")) > 0) {
						PlayerFigureValidator.mandatorySetTypes.get("m").get(1).add(typeName);
						PlayerFigureValidator.mandatorySetTypes.get("m").get(2).add(typeName);
					}
					
					if (Integer.parseInt(setTypeElement.getAttribute("mand_f_1")) > 0) {
						PlayerFigureValidator.mandatorySetTypes.get("m").get(1).add(typeName);
						PlayerFigureValidator.mandatorySetTypes.get("m").get(2).add(typeName);
					}
					
					final Map<Integer, PlayerFigureSet> setMap = new ConcurrentHashMap<>();
					final NodeList sets = setTypeElement.getElementsByTagName("set");
					
					for (int setIndex = 0; setIndex < sets.getLength(); ++setIndex) {
						final Node setNode = sets.item(setIndex);
						
						if (setNode.getNodeType() == Node.ELEMENT_NODE) {
							final Element setElement = (Element) setNode;
							final int setId = Integer.parseInt(setElement.getAttribute("id"));
							
							int colorCount = 0;
							final NodeList parts = setElement.getElementsByTagName("part");
							
							for (int partIndex = 0; partIndex < parts.getLength(); ++partIndex) {
								final Node partNode = parts.item(partIndex);
								
								if (partNode.getNodeType() == Node.ELEMENT_NODE) {
									final Element partElement = (Element) partNode;
									final int colorIndex = Integer.parseInt(partElement.getAttribute("colorindex"));
									
									if (Integer.parseInt(partElement.getAttribute("colorable")) > 0 && colorIndex > colorCount) {
										colorCount = colorIndex;
									}
								}
							}
							
							setMap.put(setId, new PlayerFigureSet(setElement.getAttribute("gender").toLowerCase(), Integer.parseInt(setElement.getAttribute("club")), Integer.parseInt(setElement.getAttribute("colorable")) > 0, Integer.parseInt(setElement.getAttribute("selectable")) > 0, colorCount));
						}
					}
					
					PlayerFigureValidator.setTypes.put(typeName, new PlayerFigureSetType(typeName, Integer.parseInt(setTypeElement.getAttribute("paletteid")), setMap));
				}
			}
		} catch (Exception e) {
			Comet.getServer().getLogger().warn("Error while initializing the PlayerFigureValidator", e);
		}
	}
	
	public static boolean isValidFigureCode(final String figureCode, final String genderCode) {
		if (!CometSettings.playerFigureValidation) {
			return false;
		}
		
		if (figureCode == null) {
			return true;
		}
		
		try {
			final String gender = "m";
			
			if (!gender.equals("m") && !gender.equals("f")) {
				return true;
			}
			
			final String[] sets = figureCode.split("\\.");
			final List<String> mandatorySets = PlayerFigureValidator.mandatorySetTypes.get(gender).get(2);
			
			if (sets.length < mandatorySets.size()) {
				return true;
			}
			
			final List<String> containedSets = new ArrayList<>();
			
			for (final String set : sets) {
				final String[] setData = set.split("-");
				
				if (setData.length < 3) {
					return true;
				}
				
				final String setType = setData[0].toLowerCase();
				
				if (!PlayerFigureValidator.setTypes.containsKey(setType)) {
					return true;
				}
				
				final PlayerFigureSetType setTypeInstance = PlayerFigureValidator.setTypes.get(setType);
				final Map<Integer, PlayerFigureSet> setMap = setTypeInstance.sets();
				final int setId = Integer.parseInt(setData[1]);
				
				if (!setMap.containsKey(setId)) {
					return true;
				}
				
				final PlayerFigureSet setInstance = setMap.get(setId);
				
				if (!setInstance.selectable() || (setData.length - 2) < setInstance.colorCount()) {
					return true;
				}
				
				for (int i = 0; i < setInstance.colorCount(); ++i) {
					final int colorId = Integer.parseInt(setData[i + 2]);
					
					if (!PlayerFigureValidator.palettes.get(setTypeInstance.paletteId()).containsKey(colorId)) {
						return true;
					}
					
					final PlayerFigureColor colorInstance = PlayerFigureValidator.palettes.get(setTypeInstance.paletteId()).get(colorId);
					
					if (!colorInstance.selectable()) {
						return true;
					}
				}
				
				containedSets.add(setType);
			}
			
			return mandatorySets.stream().anyMatch(mandatorySet -> !containedSets.contains(mandatorySet));
		} catch (final Exception ex) {
			ex.printStackTrace();
			return true;
		}
	}
	
}
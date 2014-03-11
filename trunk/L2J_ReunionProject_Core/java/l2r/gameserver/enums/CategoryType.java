/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.enums;

/**
 * This class defines all category types.
 * @author xban1x
 */
public enum CategoryType
{
	FIGHTER_GROUP,
	MAGE_GROUP,
	WIZARD_GROUP,
	CLERIC_GROUP,
	ATTACKER_GROUP,
	TANKER_GROUP,
	FIRST_CLASS_GROUP,
	SECOND_CLASS_GROUP,
	THIRD_CLASS_GROUP,
	FOURTH_CLASS_GROUP,
	BOUNTY_HUNTER_GROUP,
	WARSMITH_GROUP,
	SUMMON_NPC_GROUP,
	KNIGHT_GROUP,
	WHITE_MAGIC_GROUP,
	HEAL_GROUP,
	ASSIST_MAGIC_GROUP,
	WARRIOR_GROUP,
	HUMAN_2ND_GROUP,
	ELF_2ND_GROUP,
	DELF_2ND_GROUP,
	ORC_2ND_GROUP,
	DWARF_2ND_GROUP,
	STRIDER,
	STRIDER_GROUP,
	RED_STRIDER_GROUP,
	WOLF_GROUP,
	GROWN_UP_WOLF_GROUP,
	HATCHLING_GROUP,
	BABY_PET_GROUP,
	UPGRADE_BABY_PET_GROUP,
	WYVERN_GROUP,
	ALL_WOLF_GROUP,
	WOLF,
	SIN_EATER_GROUP,
	PET_GROUP,
	ITEM_EQUIP_PET_GROUP,
	SUBJOB_GROUP_DAGGER,
	SUBJOB_GROUP_BOW,
	SUBJOB_GROUP_KNIGHT,
	SUBJOB_GROUP_SUMMONER,
	SUBJOB_GROUP_HALF_HEALER,
	SUBJOB_GROUP_DANCE,
	SUBJOB_GROUP_WIZARD,
	HUMAN_FALL_CLASS,
	HUMAN_WALL_CLASS,
	HUMAN_MALL_CLASS,
	HUMAN_CALL_CLASS,
	ELF_FALL_CLASS,
	ELF_MALL_CLASS,
	ELF_WALL_CLASS,
	ELF_CALL_CLASS,
	DELF_FALL_CLASS,
	DELF_MALL_CLASS,
	DELF_WALL_CLASS,
	DELF_CALL_CLASS,
	ORC_FALL_CLASS,
	ORC_MALL_CLASS,
	DWARF_ALL_CLASS,
	DWARF_BOUNTY_CLASS,
	DWARF_SMITH_CLASS,
	KAMAEL_ALL_CLASS,
	KAMAEL_FIRST_CLASS_GROUP,
	KAMAEL_SECOND_CLASS_GROUP,
	KAMAEL_THIRD_CLASS_GROUP,
	KAMAEL_FOURTH_CLASS_GROUP,
	BEGINNER_FIGHTER,
	BEGINNER_MAGE,
	KAMAEL_MALE_MAIN_OCCUPATION,
	KAMAEL_FEMALE_MAIN_OCCUPATION,
	ARCHER_GROUP,
	SHIELD_MASTER,
	BARD,
	FORCE_MASTER,
	WEAPON_MASTER,
	BOW_MASTER,
	DAGGER_MASTER,
	HEAL_MASTER,
	WIZARD_MASTER,
	BUFF_MASTER,
	SUMMON_MASTER,
	WARRIOR_CLOACK,
	ROGUE_CLOACK,
	MAGE_CLOACK,
	SHIELD_MASTER2_3,
	BARD2_3,
	FORCE_MASTER2_3,
	WEAPON_MASTER2_3,
	BOW_MASTER2_3,
	DAGGER_MASTER2_3,
	HEAL_MASTER2_3,
	WIZARD_MASTER2_3,
	BUFF_MASTER2_3,
	SUMMON_MASTER2_3,
	ATTRIBUTE_GROUP_SUMMONER,
	SUB_GROUP_WARRIOR,
	SUB_GROUP_ROGUE,
	SUB_GROUP_KNIGHT,
	SUB_GROUP_SUMMONER,
	SUB_GROUP_WIZARD,
	SUB_GROUP_HEALER,
	SUB_GROUP_ENCHANTER,
	SUB_GROUP_HEC,
	SUB_GROUP_HEW,
	SUB_GROUP_HEF,
	SUB_GROUP_ORC,
	SUB_GROUP_WARE,
	SUB_GROUP_BLACK,
	SUB_GROUP_DE,
	SUB_GROUP_KAMAEL,
	LIGHT_TANKER_GROUP,
	DARK_TANKER_GROUP,
	MELEE_ATTACKER,
	RECOM_KNIGHT_GROUP,
	RECOM_MAGIC_GROUP,
	RECOM_WARRIOR_GROUP,
	RECOM_ROGUE_GROUP,
	RECOM_KAMAEL_GROUP,
	RECOM_ORCF_GROUP,
	RECOM_ORCM_GROUP,
	DEINONYCHUS_PET_GROUP,
	BEASTFARM_BEAST,
	BEASTFARM_INVADER,
	ICEQUEEN_NPC;
	
	/**
	 * Finds category by it's name
	 * @param categoryName
	 * @return A {@code CategoryType} if category was found, {@code null} if category was not found
	 */
	public static final CategoryType findByName(String categoryName)
	{
		for (CategoryType type : values())
		{
			if (type.name().equalsIgnoreCase(categoryName))
			{
				return type;
			}
		}
		return null;
	}
}

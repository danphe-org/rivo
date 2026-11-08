package com.danphe.rivo.service.internal.matchrequest.validator;

import com.danphe.rivo.common.category.Category;
import com.danphe.rivo.common.library.base.exception.AppException;
import com.danphe.rivo.common.masterdata.MasterDataDomain;
import com.danphe.rivo.common.masterdataitem.MasterDataItemDomain;
import com.danphe.rivo.repository.api.MasterDataItemRepository;
import com.danphe.rivo.repository.api.MasterDataRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MatchRequestMasterDataValidator {

  private final MasterDataRepository masterDataRepository;
  private final MasterDataItemRepository masterDataItemRepository;

  public MatchRequestMasterDataValidator(
      MasterDataRepository masterDataRepository,
      MasterDataItemRepository masterDataItemRepository) {
    this.masterDataRepository = masterDataRepository;
    this.masterDataItemRepository = masterDataItemRepository;
  }

  public record ValidatedResult(String category, String game) {}

  public ValidatedResult validate(String categoryInput, String gameInput) {
    if (categoryInput == null || categoryInput.isBlank()) {
      throw new AppException("Category is required");
    }
    if (gameInput == null || gameInput.isBlank()) {
      throw new AppException("Game is required");
    }

    String trimmedCategory = categoryInput.trim();
    String trimmedGame = gameInput.trim();

    List<MasterDataItemDomain> categoryItems = masterDataItemRepository.findByMasterDataCode("CATEGORY");
    MasterDataItemDomain matchedCategoryItem = null;
    for (MasterDataItemDomain item : categoryItems) {
      if (item.getCode() != null && item.getCode().equalsIgnoreCase(trimmedCategory)) {
        matchedCategoryItem = item;
        break;
      }
      if (item.getTitle() != null && item.getTitle().equalsIgnoreCase(trimmedCategory)) {
        matchedCategoryItem = item;
        break;
      }
      if (item.getId() != null && item.getId().equalsIgnoreCase(trimmedCategory)) {
        matchedCategoryItem = item;
        break;
      }
    }

    String canonicalCategoryCode;
    if (matchedCategoryItem != null) {
      canonicalCategoryCode = matchedCategoryItem.getCode() != null && !matchedCategoryItem.getCode().isBlank()
          ? matchedCategoryItem.getCode().toUpperCase()
          : matchedCategoryItem.getTitle().toUpperCase().replace(" ", "_");
    } else {
      Category enumCategory = Category.fromString(trimmedCategory);
      if (enumCategory != null) {
        canonicalCategoryCode = enumCategory.getCode();
      } else {
        List<MasterDataDomain> headerMatches = masterDataRepository.findByCode(trimmedCategory);
        if (!headerMatches.isEmpty()) {
          canonicalCategoryCode = headerMatches.get(0).getCode();
        } else {
          throw new AppException("Invalid category: '" + categoryInput + "'. Category must be an active category defined in master data.");
        }
      }
    }

    List<MasterDataItemDomain> validGames = resolveGamesForCategory(canonicalCategoryCode);
    if (validGames.isEmpty()) {
      throw new AppException("No games or activities are currently configured in master data for category: '" + canonicalCategoryCode + "'.");
    }

    MasterDataItemDomain matchedGame = null;
    for (MasterDataItemDomain item : validGames) {
      if (item.getId() != null && item.getId().equalsIgnoreCase(trimmedGame)) {
        matchedGame = item;
        break;
      }
      if (item.getTitle() != null && item.getTitle().equalsIgnoreCase(trimmedGame)) {
        matchedGame = item;
        break;
      }
      if (item.getCode() != null && item.getCode().equalsIgnoreCase(trimmedGame)) {
        matchedGame = item;
        break;
      }
    }

    if (matchedGame == null) {
      throw new AppException("Invalid game: '" + gameInput + "'. Game must be a valid active game/activity under category '" + canonicalCategoryCode + "' from master data.");
    }

    return new ValidatedResult(canonicalCategoryCode, matchedGame.getTitle());
  }

  public List<MasterDataItemDomain> resolveGamesForCategory(String categoryCode) {
    List<MasterDataItemDomain> games = new ArrayList<>();
    Category enumCat = Category.fromString(categoryCode);
    if (enumCat != null && enumCat.getMasterDataCode() != null) {
      games.addAll(masterDataItemRepository.findByMasterDataCode(enumCat.getMasterDataCode()));
    }
    if (games.isEmpty()) {
      games.addAll(masterDataItemRepository.findByMasterDataCode(categoryCode + "_GAMES"));
    }
    if (games.isEmpty()) {
      games.addAll(masterDataItemRepository.findByMasterDataCode(categoryCode + "_ACTIVITIES"));
    }
    if (games.isEmpty()) {
      games.addAll(masterDataItemRepository.findByMasterDataCode(categoryCode));
    }
    return games;
  }
}

package com.danphe.rivo.web.client.masterdata;

import com.danphe.rivo.common.library.base.common.Page;
import com.danphe.rivo.common.library.base.rest.RestResponse;
import com.danphe.rivo.common.masterdata.MasterDataDomain;
import com.danphe.rivo.common.masterdata.MasterDataFilter;
import com.danphe.rivo.common.masterdataitem.MasterDataItemDomain;
import com.danphe.rivo.service.api.MasterDataItemService;
import com.danphe.rivo.service.api.MasterDataService;
import com.danphe.rivo.web.client.masterdataitem.MasterDataItemResponseDto;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/client/master-data")
public class MasterDataController {

  private final MasterDataService service;
  private final MasterDataItemService itemService;

  public MasterDataController(MasterDataService service, MasterDataItemService itemService) {
    this.service = service;
    this.itemService = itemService;
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getById(@PathVariable String id) {
    MasterDataDomain domain = service.getById(id, null);
    return RestResponse.ok(MasterDataResponseDto.from(domain));
  }

  @GetMapping("/{id}/items")
  public ResponseEntity<?> getItemsByMasterDataId(@PathVariable String id) {
    List<MasterDataItemDomain> items = itemService.getByMasterDataId(id, null);
    return RestResponse.ok(items.stream().map(MasterDataItemResponseDto::from).toList());
  }

  @GetMapping("/code/{code}")
  public ResponseEntity<?> getByCode(@PathVariable String code) {
    List<MasterDataDomain> list = service.getByCode(code, null);
    return RestResponse.ok(list.stream().map(MasterDataResponseDto::from).toList());
  }

  @GetMapping("/code/{code}/items")
  public ResponseEntity<?> getItemsByCode(@PathVariable String code) {
    List<MasterDataItemDomain> items = itemService.getByMasterDataCode(code, null);
    return RestResponse.ok(items.stream().map(MasterDataItemResponseDto::from).toList());
  }

  @PostMapping("/search")
  public ResponseEntity<?> search(@RequestBody MasterDataFilter filter) {
    Page<MasterDataDomain> page = service.search(filter, null);
    return RestResponse.ok(page.map(MasterDataResponseDto::from));
  }
}

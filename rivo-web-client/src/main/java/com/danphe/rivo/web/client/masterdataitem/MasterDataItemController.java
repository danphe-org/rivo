package com.danphe.rivo.web.client.masterdataitem;

import com.danphe.rivo.common.library.base.common.Page;
import com.danphe.rivo.common.library.base.rest.RestResponse;
import com.danphe.rivo.common.masterdataitem.MasterDataItemDomain;
import com.danphe.rivo.common.masterdataitem.MasterDataItemFilter;
import com.danphe.rivo.service.api.MasterDataItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/client/master-data-items")
public class MasterDataItemController {

  private final MasterDataItemService service;

  public MasterDataItemController(MasterDataItemService service) {
    this.service = service;
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getById(@PathVariable String id) {
    MasterDataItemDomain domain = service.getById(id, null);
    return RestResponse.ok(MasterDataItemResponseDto.from(domain));
  }

  @PostMapping("/search")
  public ResponseEntity<?> search(@RequestBody MasterDataItemFilter filter) {
    Page<MasterDataItemDomain> page = service.search(filter, null);
    return RestResponse.ok(page.map(MasterDataItemResponseDto::from));
  }
}

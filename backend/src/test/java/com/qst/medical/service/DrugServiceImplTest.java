package com.qst.medical.service;

import com.qst.medical.domain.entity.Drug;
import com.qst.medical.domain.entity.Sale;
import com.qst.medical.domain.param.DrugParam;
import com.qst.medical.mapper.DrugMapper;
import com.qst.medical.mapper.SaleMapper;
import com.qst.medical.service.impl.DrugServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 药品服务单元测试：新增/修改时的 drug_sale 关联处理、删除顺序、异常分支。
 */
@ExtendWith(MockitoExtension.class)
class DrugServiceImplTest {

    @Mock
    private DrugMapper drugMapper;
    @Mock
    private SaleMapper saleMapper;

    @InjectMocks
    private DrugServiceImpl drugService;

    private DrugParam param(String name, List<Long> saleIds) {
        DrugParam p = new DrugParam();
        p.setDrugName(name);
        p.setDrugInfo("成分");
        p.setDrugEffect("功能");
        p.setDrugImg("/image/a.png");
        p.setSaleIds(saleIds);
        return p;
    }

    @Test
    @DisplayName("新增带关联药店的药品：插主表 + 插 drug_sale")
    void addDrug_withSaleIds_shouldInsertRelation() {
        when(saleMapper.selectById(1001L)).thenReturn(new Sale());
        // 模拟自增主键回填
        doAnswer(inv -> {
            Drug d = inv.getArgument(0);
            d.setDrugId(9001L);
            return 1;
        }).when(drugMapper).insertDrug(any(Drug.class));

        drugService.addDrug(param("阿莫西林", Arrays.asList(1001L)));

        ArgumentCaptor<Drug> captor = ArgumentCaptor.forClass(Drug.class);
        verify(drugMapper).insertDrug(captor.capture());
        assertEquals("阿莫西林", captor.getValue().getDrugName());
        verify(drugMapper).insertDrugSale(eq(9001L), eq(Arrays.asList(1001L)));
    }

    @Test
    @DisplayName("新增不带药店的药品：不插关联表")
    void addDrug_withoutSaleIds_shouldSkipRelation() {
        doAnswer(inv -> {
            Drug d = inv.getArgument(0);
            d.setDrugId(9002L);
            return 1;
        }).when(drugMapper).insertDrug(any(Drug.class));

        drugService.addDrug(param("维生素C", Collections.emptyList()));

        verify(drugMapper).insertDrug(any(Drug.class));
        verify(drugMapper, never()).insertDrugSale(anyLong(), anyList());
    }

    @Test
    @DisplayName("修改药品：先删旧关联再插新关联（整体替换）")
    void updateDrug_shouldReplaceRelations() {
        Drug exist = new Drug();
        exist.setDrugId(9003L);
        exist.setDrugName("旧名");
        when(drugMapper.selectById(9003L)).thenReturn(exist);
        when(saleMapper.selectById(1002L)).thenReturn(new Sale());

        drugService.updateDrug(9003L, param("新名", Arrays.asList(1002L)));

        verify(drugMapper).updateDrug(any(Drug.class));
        verify(drugMapper).deleteDrugSaleByDrugId(9003L);
        verify(drugMapper).insertDrugSale(eq(9003L), eq(Arrays.asList(1002L)));
        // 顺序：先删后插
        var order = inOrder(drugMapper);
        order.verify(drugMapper).deleteDrugSaleByDrugId(9003L);
        order.verify(drugMapper).insertDrugSale(eq(9003L), anyList());
    }

    @Test
    @DisplayName("删除药品：先清关联再删主表")
    void deleteDrug_shouldCleanRelationFirst() {
        Drug exist = new Drug();
        exist.setDrugId(9004L);
        when(drugMapper.selectById(9004L)).thenReturn(exist);

        drugService.deleteDrug(9004L);

        var order = inOrder(drugMapper);
        order.verify(drugMapper).deleteDrugSaleByDrugId(9004L);
        order.verify(drugMapper).deleteById(9004L);
    }

    @Test
    @DisplayName("删除不存在的药品抛 IllegalArgumentException")
    void deleteDrug_notExist_shouldThrow() {
        when(drugMapper.selectById(9999L)).thenReturn(null);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> drugService.deleteDrug(9999L));
        assertTrue(e.getMessage().contains("药品不存在"));
        verify(drugMapper, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("关联到不存在的药店抛 IllegalArgumentException")
    void addDrug_invalidSaleId_shouldThrow() {
        when(saleMapper.selectById(7777L)).thenReturn(null);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> drugService.addDrug(param("测试药", Arrays.asList(7777L))));
        assertTrue(e.getMessage().contains("销售地点不存在"));
        verify(drugMapper, never()).insertDrug(any(Drug.class));
    }

    @Test
    @DisplayName("重复的 saleId 会被去重后再插入")
    void addDrug_duplicateSaleIds_shouldDeduplicate() {
        when(saleMapper.selectById(1003L)).thenReturn(new Sale());
        doAnswer(inv -> {
            Drug d = inv.getArgument(0);
            d.setDrugId(9005L);
            return 1;
        }).when(drugMapper).insertDrug(any(Drug.class));

        drugService.addDrug(param("重复关联药", Arrays.asList(1003L, 1003L, 1003L)));

        verify(drugMapper).insertDrugSale(eq(9005L), eq(Arrays.asList(1003L)));
    }

    @Test
    @DisplayName("查询详情：药品不存在抛异常，存在时带出关联药店 id")
    void getDrugDetail_shouldReturnSaleIds() {
        Drug exist = new Drug();
        exist.setDrugId(9006L);
        when(drugMapper.selectById(9006L)).thenReturn(exist);
        when(drugMapper.selectSaleIdsByDrugId(9006L)).thenReturn(Arrays.asList(1001L, 1002L));

        Drug detail = drugService.getDrugDetail(9006L);

        assertEquals(2, detail.getSaleIds().size());
        assertThrows(IllegalArgumentException.class, () -> drugService.getDrugDetail(123456L));
    }
}

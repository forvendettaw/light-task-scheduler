package com.lts.admin.access.mysql;

import com.lts.admin.access.RshHandler;
import com.lts.admin.access.face.BackendJobClientMAccess;
import com.lts.admin.request.MDataPaginationReq;
import com.lts.admin.web.vo.NodeInfo;
import com.lts.core.cluster.Config;
import com.lts.monitor.access.domain.JobClientMDataPo;
import com.lts.monitor.access.mysql.MysqlJobClientMAccess;
import com.lts.store.jdbc.builder.DeleteSql;
import com.lts.store.jdbc.builder.SelectSql;
import com.lts.store.jdbc.builder.WhereSql;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 3/12/16.
 */
public class MysqlBackendJobClientMAccess extends MysqlJobClientMAccess implements BackendJobClientMAccess {

    public MysqlBackendJobClientMAccess(Config config) {
        super(config);
    }

    @Override
    public void delete(MDataPaginationReq request) {

        new DeleteSql(getSqlTemplate())
                .delete(getTableName())
                .whereSql(buildWhereSql(request))
                .doDelete();
    }

    @Override
    public List<JobClientMDataPo> querySum(MDataPaginationReq request) {
        return new SelectSql(getSqlTemplate())
                .select()
                .columns("timestamp",
                        "SUM(submit_success_num) AS submit_success_num",
                        "SUM(submit_failed_num) AS submit_failed_num",
                        "SUM(fail_store_num) AS fail_store_num",
                        "SUM(submit_fail_store_num) AS submit_fail_store_num",
                        "SUM(handle_feedback_num) AS handle_feedback_num")
                .from()
                .table(getTableName())
                .whereSql(buildWhereSql(request))
                .groupBy(" timestamp ASC ")
                .limit(request.getStart(), request.getLimit())
                .list(RshHandler.JOB_CLIENT_SUM_M_DATA_RSH);
    }

    @Override
    public List<NodeInfo> getJobClients() {
        return new SelectSql(getSqlTemplate())
                .select()
                .columns("DISTINCT identity AS identity", "node_group")
                .from()
                .table(getTableName())
                .list(RshHandler.NODE_INFO_LIST_RSH);
    }

    public WhereSql buildWhereSql(MDataPaginationReq request) {
        return new WhereSql()
                .andOnNotNull("id = ?", request.getId())
                .andOnNotEmpty("identity = ?", request.getIdentity())
                .andOnNotEmpty("node_group = ?", request.getNodeGroup())
                .andBetween("timestamp", request.getStartTime(), request.getEndTime());
    }
}

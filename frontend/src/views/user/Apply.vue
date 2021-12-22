<template>
  <v-card>
    <v-tabs
      v-model="window"
      dark
      grow
    >
      <v-tab>
        <v-icon>mdi-timer-sand-full</v-icon>
        审核中申请
      </v-tab>
      <v-tab>
        <v-icon>mdi-cloud-check</v-icon>
        已通过申请
      </v-tab>
      <v-tab>
        <v-icon>mdi-cloud-alert</v-icon>
        未通过申请
      </v-tab>
      <v-tab>
        <v-icon>mdi-menu</v-icon>
        所有申请
      </v-tab>
    </v-tabs>

    <v-window
      v-model="window"
    >

      <!-- 审核中 -->
      <v-window-item>
        <v-data-table
          :headers="getHeaders()"
          :items="review"
          :items-per-page="-1"
          hide-default-footer
        >
          <template v-slot:[`item.info`]="{ item }">
            <MessageDialog :message="item"></MessageDialog>
          </template>
        </v-data-table>
      </v-window-item>
      
      <!-- 已通过申请 -->
      <v-window-item>
        <v-data-table
          :headers="getHeaders()"
          :items="passed"
          :items-per-page="-1"
          hide-default-footer
        >
          <template v-slot:[`item.info`]="{ item }">
            <MessageDialog :message="item"></MessageDialog>
          </template>
        </v-data-table>
      </v-window-item>
      
      <!-- 未通过申请 -->
      <v-window-item>
        <v-data-table
          :headers="getHeaders()"
          :items="failed"
          :items-per-page="-1"
          hide-default-footer
        >
          <template v-slot:[`item.info`]="{ item }">
            <MessageDialog :message="item"></MessageDialog>
          </template>
        </v-data-table>
      </v-window-item>
      
      <!-- 所有申请 -->
      <v-window-item height="100%">
        <v-data-table
          :headers="getHeaders()"
          :items="all"
          :items-per-page="-1"
          hide-default-footer
          fixed-header
          group-by="type"
        >
          <template v-slot:[`item.status`]="{ item }">
            <v-chip
              v-if="item.status == '审核通过'"
              color="cyan lighten-2"
            >
              已通过
            </v-chip>
            <v-chip
              v-if="item.status == '审核不通过'"
              color="amber"
            >
              未通过
            </v-chip>
            <v-chip
              v-if="item.status == '审核中'"
              color="blue lighten-4"
            >
              审核中
            </v-chip>
          </template>
          <template v-slot:[`group.header`]="{items, isOpen, toggle}">
            <td :colspan="getHeaders().length">
              <v-icon @click="toggle">
                {{ isOpen ? 'mdi-minus' : 'mdi-plus' }}
              </v-icon>
              <span>{{items[0].type}}</span>
            </td>
          </template>
          <template v-slot:[`item.info`]="{ item }">
            <MessageDialog :message="item"></MessageDialog>
          </template>
        </v-data-table>
      </v-window-item>
    </v-window>

  </v-card>
</template>

<script>
import MessageDialog from "../../components/MessageDialog.vue"
export default {
  components: {
    MessageDialog,
  },
  data() {
    return {
      review: [
        {
          title: "人工智能",
          type: "论文",
          time: "2018-10-15",
          id: "GB123ds"
        },
        {
          title: "机器学习",
          type: "论文",
          time: "2018-10-14",
        },
        {
          title: "集群",
          type: "论文",
          time: "2018-10-16",
        },
        {
          title: "分布式",
          type: "论文",
          time: "2018-10-18",
        },
      ],
      passed: [
        {
          title: "人工智能",
          type: "论文",
          time: "2018-10-15",
        },
        {
          title: "机器学习",
          type: "论文",
          time: "2018-10-15",
        },
        {
          title: "集群",
          type: "论文",
          time: "2018-10-15",
        },
        {
          title: "分布式",
          type: "论文",
          time: "2018-10-15",
        },
      ],
      failed: [
        {
          title: "人工智能",
          type: "论文",
          time: "2018-10-15",
        },
        {
          title: "机器学习",
          type: "论文",
          time: "2018-10-15",
        },
        {
          title: "集群",
          type: "论文",
          time: "2018-10-15",
        },
        {
          title: "分布式",
          type: "论文",
          time: "2018-10-15",
        },
      ],
      all: [
        {
          title: "人工智能",
          status: "审核中",
          type: "论文",
          time: "2018-10-15",
        },
        {
          title: "机器学习",
          status: "审核中",
          type: "论文",
          time: "2018-10-15",
        },
        {
          title: "集群",
          status: "审核不通过",
          type: "论文",
          time: "2018-10-15",
        },
        {
          title: "分布式",
          status: "审核通过",
          type: "论文",
          time: "2018-10-15",
        },
        {
          title: "分布式",
          status: "审核中",
          type: "期刊",
          time: "2018-10-15",
        },
        {
          title: "人工智能",
          status: "审核中",
          type: "论文",
          time: "2018-10-15",
        },
        {
          title: "机器学习",
          status: "审核中",
          type: "论文",
          time: "2018-10-15",
        },
        {
          title: "集群",
          status: "审核不通过",
          type: "论文",
          time: "2018-10-15",
        },
        {
          title: "分布式",
          status: "审核通过",
          type: "论文",
          time: "2018-10-15",
        },
        {
          title: "分布式",
          status: "审核中",
          type: "期刊",
          time: "2018-10-15",
        },
        {
          title: "人工智能",
          status: "审核中",
          type: "论文",
          time: "2018-10-15",
        },
        {
          title: "机器学习",
          status: "审核中",
          type: "论文",
          time: "2018-10-15",
        },
        {
          title: "集群",
          status: "审核不通过",
          type: "论文",
          time: "2018-10-15",
        },
        {
          title: "分布式",
          status: "审核通过",
          type: "论文",
          time: "2018-10-15",
        },
        {
          title: "分布式",
          status: "审核中",
          type: "期刊",
          time: "2018-10-15",
        },
      ],
      window: 0,
    }
  },
  mounted() {
    this.getAllApplications();
  },
  methods: {
    getAllApplications() {
      this.$axios({
        method: "get",
        url: "/api/account/application/list",
        params: {
          page: 0,
          size: 10,
        }
      }).then(res => {
        if (res.data.success) {
          this.all = res.data.data.applications
        } else {
          console.log(res.data.message)
        }
      }).catch(error => {
        console.log(error)
      })
    },
    getReview() {
      this.$axios({
        method: "get",
        url: "/api/account/application/list",
        params: {
          page: 0,
          size: 12,
          status: "审核中",
        }
      }).then(res => {
        if (res.data.success) {
          this.review = res.data.data.applications
        } else {
          console.log(res.data.message)
        }
      }).catch(error => {
        console.log(error)
      })
    },
    getPassed() {
      this.$axios({
        method: "get",
        url: "/api/account/application/list",
        params: {
          page: 0,
          size: 12,
          status: "审核通过",
        }
      }).then(res => {
        if (res.data.success) {
          this.passed = res.data.data.applications
        } else {
          console.log(res.data.message)
        }
      }).catch(error => {
        console.log(error)
      })
    },
    getFailed() {
      this.$axios({
        method: "get",
        url: "/api/account/application/list",
        params: {
          page: 0,
          size: 12,
          status: "审核不通过",
        }
      }).then(res => {
        if (res.data.success) {
          this.failed = res.data.data.applications
        } else {
          console.log(res.data.message)
        }
      }).catch(error => {
        console.log(error)
      })
    },
    getBrief() {
      this.$axios({
        method: "post",
        url: "/api/search/info/brief",
        data: {
          entity: "paper",
          ids: []
        }
      }).then(res => {
        console.log(res.data)
      }).catch(error => {
        console.log(error)
      })
    },
    getHeaders() {
      var headers = []
      if (this.window != 3) {
        headers = [
          {
            text: "标题",
            value: "title",
            align: "start",
            width: "40%",
            sortable: false,
            class: "grey lighten-1 text-body-2 font-weight-black"
          },
          {
            text: "种类",
            value: "type",
            align: "start",
            sortable: false,
            class: "grey lighten-1 text-body-2 font-weight-black"
          },
          {
            text: "申请时间",
            value: "time",
            align: "start",
            class: "grey lighten-1 text-body-2 font-weight-black"
          },
          {
            text: "详细信息",
            value: "info",
            align: "center",
            sortable: false,
            width: "120px",
            class: "grey lighten-1 text-body-2 font-weight-black"
          },
        ]
      } else {
        headers = [
          {
            text: "标题",
            value: "title",
            align: "start",
            width: "40%",
            sortable: false,
            class: "grey lighten-1 text-body-2 font-weight-black"
          },
          {
            text: "状态",
            value: "status",
            align: "start",
            sortable: false,
            class: "grey lighten-1 text-body-2 font-weight-black"
          },
          {
            text: "种类",
            value: "type",
            align: "start",
            sortable: false,
            class: "grey lighten-1 text-body-2 font-weight-black"
          },
          {
            text: "申请时间",
            value: "time",
            align: "start",
            class: "grey lighten-1 text-body-2 font-weight-black"
          },
          {
            text: "详细信息",
            value: "info",
            align: "center",
            sortable: false,
            width: "120px",
            class: "grey lighten-1 text-body-2 font-weight-black"
          },
        ]
      }
      return headers;
    }
  }
}
</script>

<style scoped>
.v-data-table-header {
  color: grey;
}
</style>
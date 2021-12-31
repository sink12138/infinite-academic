<template>
  <div class="center">
    <v-card min-width="800" flat outlined>
      <v-tabs v-model="window" grow color="black">
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
      <v-window v-model="window">
        <!-- 审核中 -->
        <v-window-item>
          <v-data-table
            :headers="getHeaders()"
            :items="review"
            :items-per-page="-1"
            hide-default-footer
            fixed-header
            height="600"
          >
            <template v-slot:[`item.info`]="{ item }">
              <MessageDialog :message="item"></MessageDialog>
            </template>
            <template v-slot:no-data>
              <v-card-text>无申请信息</v-card-text>
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
            fixed-header
            height="600"
          >
            <template v-slot:[`item.info`]="{ item }">
              <MessageDialog :message="item"></MessageDialog>
            </template>
            <template v-slot:no-data>
              <v-card-text>无申请信息</v-card-text>
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
            fixed-header
            height="600"
          >
            <template v-slot:[`item.info`]="{ item }">
              <MessageDialog :message="item"></MessageDialog>
            </template>
            <template v-slot:no-data>
              <v-card-text>无申请信息</v-card-text>
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
            height="600"
          >
            <template v-slot:[`item.status`]="{ item }">
              <v-chip v-if="item.status == '审核通过'" color="cyan lighten-2">
                已通过
              </v-chip>
              <v-chip v-if="item.status == '审核不通过'" color="amber">
                未通过
              </v-chip>
              <v-chip v-if="item.status == '审核中'" color="blue lighten-4">
                审核中
              </v-chip>
            </template>
            <template v-slot:[`item.info`]="{ item }">
              <MessageDialog :message="item"></MessageDialog>
            </template>
            <template v-slot:no-data>
              <v-card-text>无申请信息</v-card-text>
            </template>
          </v-data-table>
        </v-window-item>
      </v-window>
    </v-card>
  </div>
</template>

<script>
import MessageDialog from "../../components/MessageDialog.vue";
export default {
  components: {
    MessageDialog,
  },
  data() {
    return {
      review: [],
      passed: [],
      failed: [],
      all: [],
      window: 0,
    };
  },
  mounted() {
    this.getAllApplications();
    this.getReview();
    this.getPassed();
    this.getFailed();
  },
  methods: {
    getAllApplications() {
      this.$axios({
        method: "get",
        url: "/api/account/application/list",
        params: {
          page: 0,
          size: 20,
        },
      })
        .then((res) => {
          if (res.data.success) {
            this.all = res.data.data.applications;
            this.all.forEach(function (item) {
              if (item.type.substring(2) == "论文") {
                this.$axios({
                  method: "get",
                  url: "/api/account/application/details/" + this.item.id,
                })
                  .then((res) => {
                    if (res.data.success) {
                      if (item.type == "添加论文")
                        item.title = res.data.data.add.title;
                      if (item.type == "修改论文")
                        item.title = res.data.data.edit.title;
                      if (item.type == "移除论文") {
                        this.$axios({
                          method: "post",
                          url: "/api/search/info/brief",
                          data: {
                            entity: "paper",
                            ids: [res.data.data.paperId],
                          },
                        })
                          .then((res) => {
                            if (res.data.success) {
                              item.title = res.data.data[0].name;
                            } else {
                              console.log(res.data.message);
                            }
                          })
                          .catch((error) => {
                            console.log(error);
                          });
                      }
                    } else {
                      console.log(res.data.message);
                    }
                  })
                  .catch((error) => {
                    console.log(error);
                  });
              } else {
                item.title = item.type;
              }
            });
          } else {
            console.log(res.data.message);
          }
        })
        .catch((error) => {
          console.log(error);
        });
    },
    getReview() {
      this.$axios({
        method: "get",
        url: "/api/account/application/list",
        params: {
          page: 0,
          size: 20,
          status: "审核中",
        },
      })
        .then((res) => {
          if (res.data.success) {
            this.review = res.data.data.applications;
            this.review.forEach(function (item) {
              if (item.type.substring(2) == "论文") {
                this.$axios({
                  method: "get",
                  url: "/api/account/application/details/" + this.item.id,
                })
                  .then((res) => {
                    if (res.data.success) {
                      if (item.type == "添加论文")
                        item.title = res.data.data.add.title;
                      if (item.type == "修改论文")
                        item.title = res.data.data.edit.title;
                      if (item.type == "移除论文") {
                        this.$axios({
                          method: "post",
                          url: "/api/search/info/brief",
                          data: {
                            entity: "paper",
                            ids: [res.data.data.paperId],
                          },
                        })
                          .then((res) => {
                            if (res.data.success) {
                              item.title = res.data.data[0].name;
                            } else {
                              console.log(res.data.message);
                            }
                          })
                          .catch((error) => {
                            console.log(error);
                          });
                      }
                    } else {
                      console.log(res.data.message);
                    }
                  })
                  .catch((error) => {
                    console.log(error);
                  });
              } else {
                item.title = item.type;
              }
            });
          } else {
            console.log(res.data.message);
          }
        })
        .catch((error) => {
          console.log(error);
        });
    },
    getPassed() {
      this.$axios({
        method: "get",
        url: "/api/account/application/list",
        params: {
          page: 0,
          size: 20,
          status: "审核通过",
        },
      })
        .then((res) => {
          if (res.data.success) {
            this.passed = res.data.data.applications;
            this.passed.forEach(function (item) {
              if (item.type.substring(2) == "论文") {
                this.$axios({
                  method: "get",
                  url: "/api/account/application/details/" + this.item.id,
                })
                  .then((res) => {
                    if (res.data.success) {
                      if (item.type == "添加论文")
                        item.title = res.data.data.add.title;
                      if (item.type == "修改论文")
                        item.title = res.data.data.edit.title;
                      if (item.type == "移除论文") {
                        this.$axios({
                          method: "post",
                          url: "/api/search/info/brief",
                          data: {
                            entity: "paper",
                            ids: [res.data.data.paperId],
                          },
                        })
                          .then((res) => {
                            if (res.data.success) {
                              item.title = res.data.data[0].name;
                            } else {
                              console.log(res.data.message);
                            }
                          })
                          .catch((error) => {
                            console.log(error);
                          });
                      }
                    } else {
                      console.log(res.data.message);
                    }
                  })
                  .catch((error) => {
                    console.log(error);
                  });
              } else {
                item.title = item.type;
              }
            });
          } else {
            console.log(res.data.message);
          }
        })
        .catch((error) => {
          console.log(error);
        });
    },
    getFailed() {
      this.$axios({
        method: "get",
        url: "/api/account/application/list",
        params: {
          page: 0,
          size: 20,
          status: "审核不通过",
        },
      })
        .then((res) => {
          if (res.data.success) {
            this.failed = res.data.data.applications;
            this.failed.forEach(function (item) {
              if (item.type.substring(2) == "论文") {
                this.$axios({
                  method: "get",
                  url: "/api/account/application/details/" + this.item.id,
                })
                  .then((res) => {
                    if (res.data.success) {
                      if (item.type == "添加论文")
                        item.title = res.data.data.add.title;
                      if (item.type == "修改论文")
                        item.title = res.data.data.edit.title;
                      if (item.type == "移除论文") {
                        this.$axios({
                          method: "post",
                          url: "/api/search/info/brief",
                          data: {
                            entity: "paper",
                            ids: [res.data.data.paperId],
                          },
                        })
                          .then((res) => {
                            if (res.data.success) {
                              item.title = res.data.data[0].name;
                            } else {
                              console.log(res.data.message);
                            }
                          })
                          .catch((error) => {
                            console.log(error);
                          });
                      }
                    } else {
                      console.log(res.data.message);
                    }
                  })
                  .catch((error) => {
                    console.log(error);
                  });
              } else {
                item.title = item.type;
              }
            });
          } else {
            console.log(res.data.message);
          }
        })
        .catch((error) => {
          console.log(error);
        });
    },
    getBrief() {
      this.$axios({
        method: "post",
        url: "/api/search/info/brief",
        data: {
          entity: "paper",
          ids: [],
        },
      })
        .then((res) => {
          console.log(res.data);
        })
        .catch((error) => {
          console.log(error);
        });
    },
    getHeaders() {
      var headers = [];
      if (this.window != 3) {
        headers = [
          {
            text: "标题",
            value: "title",
            align: "start",
            width: "40%",
            sortable: false,
            class: "text-body-1 font-weight-black",
          },
          {
            text: "种类",
            value: "type",
            align: "start",
            sortable: true,
            class: "text-body-1 font-weight-black",
          },
          {
            text: "申请时间",
            value: "time",
            align: "start",
            class: "text-body-1 font-weight-black",
          },
          {
            text: "详细信息",
            value: "info",
            align: "center",
            sortable: false,
            width: "120px",
            class: "text-body-1 font-weight-black",
          },
        ];
      } else {
        headers = [
          {
            text: "标题",
            value: "title",
            align: "start",
            width: "40%",
            sortable: false,
            class: "text-body-1 font-weight-black",
          },
          {
            text: "状态",
            value: "status",
            align: "center",
            sortable: false,
            class: "text-body-1 font-weight-black",
          },
          {
            text: "种类",
            value: "type",
            align: "start",
            sortable: true,
            class: "text-body-1 font-weight-black",
          },
          {
            text: "申请时间",
            value: "time",
            align: "start",
            class: "text-body-1 font-weight-black",
          },
          {
            text: "详细信息",
            value: "info",
            align: "center",
            sortable: false,
            width: "120px",
            class: "text-body-1 font-weight-black",
          },
        ];
      }
      return headers;
    },
  },
};
</script>

<style scoped>
.center {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
}
</style>
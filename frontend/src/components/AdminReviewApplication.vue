<template>
  <v-card flat outlined>
    <v-card-text>
      <v-data-table
        :headers="headers"
        :items="applications"
        :options.sync="options"
        :server-items-length="totalApplications"
        :loading="loading"
        class="applications"
        show-select
        v-model="selectedItem"
        show-expand
        item-key="id"
        style="word-break: break-all"
      >
        <template v-slot:top>
          <v-toolbar flat>
            <v-toolbar-title>审核申请</v-toolbar-title>
            <v-divider inset vertical></v-divider>
            <v-btn outlined @click="passItems" class="ml-4">
              <v-icon dark> mdi-check </v-icon>
              一键通过
            </v-btn>
            <v-spacer></v-spacer>

            <v-btn-toggle v-model="radioGroup" @change="getApplications" group>
              <v-btn
                v-for="item in radio"
                :key="item.title"
                v-html="'<b>' + item.title + '</b>'"
                :value="item.num"
              ></v-btn>
            </v-btn-toggle>

            <v-divider inset vertical></v-divider>

            <v-col cols="12" sm="3">
              <v-select
                v-model="select"
                :items="items"
                item-text="text"
                item-value="num"
                single-line
                hide-details
                return-object
                :menu-props="{ maxHeight: 360 }"
                dense
                @change="getApplications"
                label="申请类型"
                solo
              ></v-select>
            </v-col>

            <v-dialog v-model="dialogFail" max-width="500px">
              <v-card>
                <v-card-title class="text-h5"
                  >真的要不通过这个申请吗？</v-card-title
                >
                <v-card-text>
                  <v-textarea
                    outlined
                    v-model="failReason"
                    :counter="300"
                    label="拒绝原因"
                    height="250"
                  ></v-textarea>
                </v-card-text>
                <v-card-actions>
                  <v-spacer></v-spacer>
                  <v-btn color="blue darken-1" text @click="closeFail"
                    >再想想</v-btn
                  >
                  <v-btn color="blue darken-1" text @click="fail">确定</v-btn>
                  <v-spacer></v-spacer>
                </v-card-actions>
              </v-card>
            </v-dialog>
          </v-toolbar>
        </template>

        <template v-slot:[`expanded-item`]="{ headers, item }">
          <td :colspan="headers.length">
            <v-row>
              <v-col cols="2"> 链接： </v-col>
              <v-col>
                {{ item.websiteLink }}
              </v-col>
            </v-row>
            <v-divider></v-divider>
            <v-row>
              <v-col cols="2"> 文件Token： </v-col>
              <v-col>
                {{ item.fileToken }}
              </v-col>
              <v-col cols="2">
                <v-btn outlined x-small @click="download(item.fileToken)">
                  <v-icon small>mdi-download</v-icon>
                  下载
                </v-btn>
              </v-col>
            </v-row>
          </td>
        </template>

        <template v-slot:[`item.actions`]="{ item }">
          <v-icon v-if="item.status == '审核中'" @click="passItem(item)">
            mdi-check-circle-outline
          </v-icon>
          <v-icon v-if="item.status == '审核中'" @click="failItem(item)">
            mdi-close-circle-outline
          </v-icon>

          <Application :message="item" path="admin"></Application>
        </template>

        <template v-slot:no-data>
          <v-btn color="primary" @click="refresh" rounded> 重置 </v-btn>
        </template>
      </v-data-table>
    </v-card-text>
  </v-card>
</template>

<script>
import Application from "../components/MessageDialog.vue";

export default {
  components: {
    Application,
  },

  data() {
    return {
      dialog: false,
      dialogFail: false,
      totalApplications: 0,
      options: {},
      loading: true,
      page: 0,
      size: 10,
      headers: [
        {
          text: "申请ID",
          align: "start",
          value: "id",
          sortable: false,
          width: "18%",
        },
        { text: "申请类型", value: "type", sortable: false, width: "8%" },
        { text: "申请人ID", value: "userId", sortable: false, width: "18%" },
        { text: "申请时间", value: "time", sortable: false, width: "12%" },
        { text: "邮箱", value: "email", sortable: false, width: "15%" },
        { text: "当前状态", value: "status", sortable: false, width: "9%" },
        { text: "操作", value: "actions", sortable: false, width: "12%" },
      ],
      applications: [
        {
          id: 0,
          type: "test",
          userId: 2,
          time: "2021.12.15",
          email: "test",
          websiteLink: "test",
          fileToken: "test",
          status: "test",
        },
      ],
      radioGroup: 0,
      radio: [
        {
          num: 0,
          title: "全部",
        },
        {
          num: 1,
          title: "审核中",
        },
        {
          num: 2,
          title: "审核不通过",
        },
        {
          num: 3,
          title: "审核通过",
        },
      ],
      select: { text: "全部", num: "0" },
      items: [
        { text: "全部", num: "0" },
        { text: "学者认证", num: "1" },
        { text: "门户认领", num: "2" },
        { text: "门户信息修改", num: "3" },
        { text: "添加论文", num: "4" },
        { text: "下架论文", num: "5" },
        { text: "修改论文信息", num: "6" },
        { text: "专利转让", num: "7" },
      ],
      selectedItem: [],
      refreshtime: 2,
      failReason: "",
    };
  },
  computed: {},

  watch: {
    options: {
      handler() {
        this.getApplications();
      },
      deep: true,
    },
  },

  created() {},

  methods: {
    getApplications() {
      this.loading = true;
      this.page = this.options.page;
      this.size =
        this.options.itemsPerPage <= 30 ? this.options.itemsPerPage : 30;

      if (this.radioGroup == 0 && this.select["num"] == 0) {
        this.$axios({
          method: "get",
          url: "api/admin/review/list",
          params: {
            page: this.page - 1,
            size: this.size,
          },
        }).then((response) => {
          console.log(response.data);
          if (response.data.success === true) {
            this.applications = response.data.data.items;
            this.totalApplications = response.data.data.pageCount * this.size;
            this.loading = false;
          } else {
            this.$notify({
              title: "失败",
              message: "账户信息获取失败",
              type: "warning",
            });
            this.loading = false;
          }
        });
      } else if (this.radioGroup == 0 && this.select["num"] != 0) {
        this.$axios({
          method: "get",
          url: "api/admin/review/list",
          params: {
            page: this.page - 1,
            size: this.size,
            type: this.select["text"],
          },
        }).then((response) => {
          console.log(response.data);
          if (response.data.success === true) {
            this.applications = response.data.data.items;
            this.totalApplications = response.data.data.pageCount * this.size;
            this.loading = false;
          } else {
            this.$notify({
              title: "失败",
              message: "账户信息获取失败",
              type: "warning",
            });
            this.loading = false;
          }
        });
      } else if (this.radioGroup != 0 && this.select["num"] == 0) {
        this.$axios({
          method: "get",
          url: "api/admin/review/list",
          params: {
            page: this.page - 1,
            size: this.size,
            status: this.radio[this.radioGroup]["title"],
          },
        }).then((response) => {
          console.log(response.data);
          if (response.data.success === true) {
            this.applications = response.data.data.items;
            this.totalApplications = response.data.data.pageCount * this.size;
            this.loading = false;
          } else {
            this.$notify({
              title: "失败",
              message: "账户信息获取失败",
              type: "warning",
            });
            this.loading = false;
          }
        });
      } else {
        this.$axios({
          method: "get",
          url: "api/admin/review/list",
          params: {
            page: this.page - 1,
            size: this.size,
            type: this.select["text"],
            status: this.radio[this.radioGroup]["title"],
          },
        }).then((response) => {
          console.log(response.data);
          if (response.data.success === true) {
            this.applications = response.data.data.items;
            this.totalApplications = response.data.data.pageCount * this.size;
            this.loading = false;
          } else {
            this.$notify({
              title: "失败",
              message: "账户信息获取失败",
              type: "warning",
            });
            this.loading = false;
          }
        });
      }
      this.checkedItem = this.applications[0];
    },

    checkItem(item) {
      this.checkedItem = item;
      this.dialog = true;
    },

    passItem(item) {
      if (item.type === "学者认证") {
        this.$axios({
          method: "post",
          url: "api/admin/review/accept/certification",
          params: {
            id: item.id,
          },
        }).then((response) => {
          console.log(response.data);
          if (response.data.success === true) {
            this.$notify({
              title: "成功",
              message: "审核成功",
              type: "success",
            });
          } else {
            this.$notify({
              title: "失败",
              message: "审核失败",
              type: "warning",
            });
          }
        });
      } else if (item.type === "门户认领") {
        this.$axios({
          method: "post",
          url: "api/admin/review/accept/claim",
          params: {
            id: item.id,
          },
        }).then((response) => {
          console.log(response.data);
          if (response.data.success === true) {
            this.$notify({
              title: "成功",
              message: "审核成功",
              type: "success",
            });
          } else {
            this.$notify({
              title: "失败",
              message: "审核失败",
              type: "warning",
            });
          }
        });
      } else if (item.type === "门户修改") {
        this.$axios({
          method: "post",
          url: "api/admin/review/accept/modification",
          params: {
            id: item.id,
          },
        }).then((response) => {
          console.log(response.data);
          if (response.data.success === true) {
            this.$notify({
              title: "成功",
              message: "审核成功",
              type: "success",
            });
          } else {
            this.$notify({
              title: "失败",
              message: "审核失败",
              type: "warning",
            });
          }
        });
      } else if (item.type === "添加论文") {
        this.$axios({
          method: "post",
          url: "api/admin/review/accept/paper/add",
          params: {
            id: item.id,
          },
        }).then((response) => {
          console.log(response.data);
          if (response.data.success === true) {
            this.$notify({
              title: "成功",
              message: "审核成功",
              type: "success",
            });
          } else {
            this.$notify({
              title: "失败",
              message: "审核失败",
              type: "warning",
            });
          }
        });
      } else if (item.type === "修改论文") {
        this.$axios({
          method: "post",
          url: "api/admin/review/accept/paper/edit",
          params: {
            id: item.id,
          },
        }).then((response) => {
          console.log(response.data);
          if (response.data.success === true) {
            this.$notify({
              title: "成功",
              message: "审核成功",
              type: "success",
            });
          } else {
            this.$notify({
              title: "失败",
              message: "审核失败",
              type: "warning",
            });
          }
        });
      } else if (item.type === "移除论文") {
        this.$axios({
          method: "post",
          url: "api/admin/review/accept/paper/remove",
          params: {
            id: item.id,
          },
        }).then((response) => {
          console.log(response.data);
          if (response.data.success === true) {
            this.$notify({
              title: "成功",
              message: "审核成功",
              type: "success",
            });
          } else {
            this.$notify({
              title: "失败",
              message: "审核失败",
              type: "warning",
            });
          }
        });
      } else if (item.type === "专利转让") {
        this.$axios({
          method: "post",
          url: "api/admin/review/accept/transfer",
          params: {
            id: item.id,
          },
        }).then((response) => {
          console.log(response.data);
          if (response.data.success === true) {
            this.$notify({
              title: "成功",
              message: "审核成功",
              type: "success",
            });
          } else {
            this.$notify({
              title: "失败",
              message: "审核失败",
              type: "warning",
            });
          }
        });
      }
      this.timer = setInterval(() => {
        this.refreshtime--;

        if (this.refreshtime === 0) {
          this.refreshtime = 2;
          this.getApplications();
          clearInterval(this.timer);
        }
      }, 1000);
    },

    failItem(item) {
      this.checkedItem = item;
      this.failReason = "";
      this.dialogFail = true;
    },

    fail() {
      this.$axios({
        method: "post",
        url: "api/admin/review/reject",
        params: {
          id: this.checkedItem.id,
          reason: this.failReason,
        },
      }).then((response) => {
        console.log(response.data);
        if (response.data.success === true) {
          this.$notify({
            title: "成功",
            message: "审核成功",
            type: "warning",
          });
        } else {
          this.$notify({
            title: "失败",
            message: "审核失败",
            type: "warning",
          });
        }
      });
      this.closeFail();
    },

    close() {
      this.dialog = false;
      this.content = "";

      this.timer = setInterval(() => {
        this.refreshtime--;

        if (this.refreshtime === 0) {
          this.refreshtime = 2;
          this.getApplications();
          clearInterval(this.timer);
        }
      }, 1000);
    },

    closeFail() {
      this.dialogFail = false;
      this.timer = setInterval(() => {
        this.refreshtime--;

        if (this.refreshtime === 0) {
          this.refreshtime = 2;
          this.getApplications();
          clearInterval(this.timer);
        }
      }, 1000);
    },

    passItems() {
      for (var i in this.selectedItem) {
        this.passItem(this.selectedItem[i]);
      }
    },

    download(token) {
      console.log(token);
      this.$axios({
        method: "get",
        url: "/api/resource/download",
        params: { token: token },
        responseType: "blob",
      }).then(
        (response) => {
          console.log(response);
          const filename = decodeURIComponent(
            response.headers["content-disposition"].split(";")[1].split("=")[1]
          );
          console.log(filename);
          this.load(response.data, filename);
          this.$notify({
            title: "文件",
            message: "下载中",
            type: "success",
          });
        },
        (err) => {
          alert(err);
        }
      );
    },

    load(data, filename) {
      if (!data) {
        return;
      }
      let url = window.URL.createObjectURL(
        new Blob([data], { type: "application/json;charset=utf-8" })
      );
      let link = document.createElement("a");
      link.style.display = "none";
      link.href = url;
      link.download = filename;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    },
  },
};
</script>

<style>
.passAll {
  background: -webkit-gradient(
    linear,
    100% 0%,
    0% 0%,
    from(#59c2fd),
    to(#2b99ff)
  );
  border-radius: 20px;
  color: #fff;
}
</style>
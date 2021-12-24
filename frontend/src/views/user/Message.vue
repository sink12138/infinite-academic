<template>
  <div class="center">
    <v-card
      flat
      outlined
      min-width="1000"
      min-height="650"
    >
      <v-card-text>
        <v-data-table
          :headers="headers"
          :items="messages"
          :page.sync="page"
          :items-per-page="size"
          hide-default-footer
          class="elevation-0"
          @page-count="pageCount = $event"
          height="100%"
        >
          <template v-slot:top>
            <v-toolbar flat>
              <v-toolbar-title>信息列表</v-toolbar-title>
              <v-divider
                inset
                vertical
              ></v-divider>
              <v-spacer></v-spacer>
              <v-btn
                color="primary"
                @click="dialogReadAll=true"
              >
                <v-icon>mdi-delete</v-icon>
                一键已读
              </v-btn>&emsp;
              <v-btn
                color="error"
                @click="dialogDeleteAll = true"
              >
                <v-icon>mdi-delete</v-icon>
                删除所有信息
              </v-btn>&emsp;
              <v-switch
                v-model="isChooseNotRead"
                inset
                hide-details
                @click="reloadNotRead()"
                label="仅显示未读消息"
              ></v-switch>
              <v-dialog
                v-model="dialogDelete"
                max-width="500px"
              >
                <v-card>
                  <v-card-title class="text-h5">真的要删除这条消息吗？</v-card-title>
                  <v-card-actions>
                    <v-spacer></v-spacer>
                    <v-btn
                      color="blue darken-1"
                      text
                      @click="closeDelete()"
                    >否</v-btn>
                    <v-btn
                      color="blue darken-1"
                      text
                      @click="submitDelete()"
                    >是</v-btn>
                    <v-spacer></v-spacer>
                  </v-card-actions>
                </v-card>
              </v-dialog>
              <v-dialog
                v-model="dialogDeleteAll"
                max-width="500px"
              >
                <v-card>
                  <v-card-title class="text-h5">真的要删除全部消息吗？</v-card-title>
                  <v-card-actions>
                    <v-spacer></v-spacer>
                    <v-btn
                      color="blue darken-1"
                      text
                      @click="closeDeleteAll()"
                    >否</v-btn>
                    <v-btn
                      color="blue darken-1"
                      text
                      @click="allDelete()"
                    >是</v-btn>
                    <v-spacer></v-spacer>
                  </v-card-actions>
                </v-card>
              </v-dialog>
              <v-dialog
                v-model="dialogReadAll"
                max-width="500px"
              >
                <v-card>
                  <v-card-title class="text-h5">真的要将所有信息设置为已读吗？</v-card-title>
                  <v-card-actions>
                    <v-spacer></v-spacer>
                    <v-btn
                      color="blue darken-1"
                      text
                      @click="closeReadAll()"
                    >否</v-btn>
                    <v-btn
                      color="blue darken-1"
                      text
                      @click="allRead()"
                    >是</v-btn>
                    <v-spacer></v-spacer>
                  </v-card-actions>
                </v-card>
              </v-dialog>
            </v-toolbar>
          </template>
          <template v-slot:[`item.actions`]="{ item }">
            <v-icon @click="openDetail(item)">mdi-information-outline</v-icon>&emsp;&emsp;&emsp;
            <v-icon @click="deleteItem(item)"> mdi-delete </v-icon>
          </template>
          <template v-slot:no-data>
            <h3 class="no-data">
              暂无消息,点击
              <v-btn
                @click="getMessages(0)"
                text
              > 刷新 </v-btn>
            </h3>
          </template>
        </v-data-table>
      </v-card-text>
    </v-card>
    <v-pagination
      v-model="page"
      :length="pageCount"
      @click="this.getMessages(num)"
    ></v-pagination>
    <v-dialog
      v-model="details"
      width="800"
    >
      <v-card class="text-left">
        <v-card-title
          class="headline grey lighten-2"
          primary-title
        >
          {{ detail.title }}
        </v-card-title>
        <v-row no-gutters>
          <v-col cols="4">
            <v-card-text class="font-weight-black"> 消息时间: </v-card-text>
          </v-col>
          <v-col cols="8">
            <v-card-text>
              {{ detail.time }}
            </v-card-text>
          </v-col>
        </v-row>
        <v-row no-gutters>
          <v-col cols="4">
            <v-card-text class="font-weight-black"> 正文: </v-card-text>
          </v-col>
          <v-col cols="8">
            <v-card-text>
              {{ detail.content }}
            </v-card-text>
          </v-col>
        </v-row>
        <v-divider></v-divider>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn
            color="primary"
            text
            @click="returnList()"
          > 关闭 </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script>
export default {
  mounted() {
    this.$axios({
      method: "get",
      url: "/api/account/message/count",
    }).then((response) => {
      console.log(response.data);
      if (response.data.success) {
        this.totalNotReadMessages = response.data.data;
      }
    });
    this.getMessages();
    this.getMessages();
  },
  data() {
    return {
      isChooseRead: false,
      isChooseNotRead: false,
      dialog: false,
      dialogReadAll: false,
      dialogDeleteAll: false,
      dialogDelete: false,
      dialogValid: false,
      totalNotReadMessages: 0,
      totalMessages: 0,
      loading: true,
      page: 1,
      read: false,
      size: 10,
      details: false,
      headers: [
        {
          text: "消息时间",
          value: "time",
          sortable: false,
          class: "text-body-1 font-weight-black",
        },
        {
          text: "消息标题",
          value: "title",
          sortable: false,
          class: "text-body-1 font-weight-black",
        },
        {
          text: "操作",
          value: "actions",
          sortable: false,
          class: "text-body-1 font-weight-black",
        },
      ],
      messages: [],
      readList: [],
      deleteMessage: {
        id: "",
        title: "",
        time: "",
        content: "",
        read: true,
      },
      detail: {
        id: "",
        title: "",
        time: "",
        content: "",
        read: false,
      },
      defaultItem: {
        id: "",
        title: "",
        time: "",
        content: "",
        read: false,
      },
      deleteMessages: [],
      pageCount: 0,
      itemsPerPage: 10,
    };
  },
  watch: {
    dialog(val) {
      val || this.close();
    },
    dialogDelete(val) {
      val || this.closeDelete();
    },
  },
  methods: {
    allDelete() {
      this.$axios({
        method: "post",
        url: "/api/account/message/remove",
        data: {
          idList: [],
        },
      }).then((response) => {
        console.log(response.data);
        if (response.data.success === true) {
          this.$notify({
            title: "成功",
            message: "信息删除成功",
            type: "success",
          });
          this.getMessages();
          this.getMessages();
        } else {
          this.$notify({
            title: "失败",
            message: "信息删除失败",
            type: "warning",
          });
        }
      });
      this.dialogDeleteAll = false;
      this.getMessages();
    },
    allRead() {
      this.$axios({
        method: "post",
        url: "/api/account/message/read",
        data: {
          idList: [],
        },
      }).then((response) => {
        console.log(response.data);
        if (response.data.success === true) {
          this.$notify({
            title: "成功",
            message: "全部信息已读",
            type: "success",
          });
        }
        this.dialogReadAll = false;
        this.getMessages();
        this.detail = this.defaultItem;
      });
    },
    reloadRead() {
      if (this.isChooseNotRead === true) {
        this.isChooseNotRead = false;
      }
      this.read = true;
      this.getMessages();
      if (this.isChooseRead === false && this.isChooseNotRead === false) {
        this.getMessages();
      }
    },
    reloadNotRead() {
      if (this.isChooseRead === true) {
        this.isChooseRead = false;
      }
      this.read = false;
      this.getMessages();
      if (this.isChooseRead === false && this.isChooseNotRead === false) {
        this.getMessages();
      }
    },
    submitDelete() {
      this.deleteMessages.push(this.deleteMessage.id);
      console.log(this.deleteMessages);
      this.$axios({
        method: "post",
        url: "/api/account/message/remove",
        data: {
          idList: this.deleteMessages,
        },
      }).then((response) => {
        console.log(response.data);
        if (response.data.success === true) {
          this.$notify({
            title: "成功",
            message: "信息删除成功",
            type: "success",
          });
        } else {
          this.$notify({
            title: "失败",
            message: "信息删除失败",
            type: "warning",
          });
        }
      });
      this.closeDelete();
      this.getMessages();
    },
    getMessages(num) {
      if (num == 0)
        this.$notify({
          title: "消息已刷新",
          type: "info",
        });
      if (this.isChooseRead === false && this.isChooseNotRead === false) {
        this.loading = true;
        this.$axios({
          method: "get",
          url: "/api/account/message/list",
          params: {
            page: this.page - 1,
            size: this.size,
          },
        }).then((response) => {
          console.log(response.data);
          if (response.data.success === true) {
            this.messages = response.data.data.messages;
            this.pageCount = response.data.data.totalPages;
            this.loading = false;
          } else {
            this.$notify({
              title: "失败",
              message: "信息获取失败",
              type: "warning",
            });
            this.loading = false;
          }
        });
      } else if (this.isChooseRead === true) {
        this.loading = true;
        this.$axios({
          method: "get",
          url: "/api/account/message/list",
          params: {
            page: this.page - 1,
            size: this.size,
            read: true,
          },
        }).then((response) => {
          console.log(response.data);
          if (response.data.success === true) {
            this.messages = response.data.data.messages;
            this.totalMessages = response.data.data.totalPages * this.size;
            this.loading = false;
          } else {
            this.$notify({
              title: "失败",
              message: "信息获取失败",
              type: "warning",
            });
            this.loading = false;
          }
        });
      } else {
        this.loading = true;
        this.$axios({
          method: "get",
          url: "/api/account/message/list",
          params: {
            page: this.page - 1,
            size: this.size,
            read: false,
          },
        }).then((response) => {
          console.log(response.data);
          if (response.data.success === true) {
            this.messages = response.data.data.messages;
            this.totalMessages = response.data.data.totalPages * this.size;
            this.loading = false;
          } else {
            this.$notify({
              title: "失败",
              message: "信息获取失败",
              type: "warning",
            });
            this.loading = false;
          }
        });
      }
    },
    returnList() {
      this.details = false;
      this.detail.read = true;
      this.readList.push(this.detail.id);
      console.log(this.detail);
      this.$axios({
        method: "post",
        url: "/api/account/message/read",
        data: {
          idList: this.readList,
        },
      }).then((response) => {
        console.log(response.data);
        this.getMessages();
        this.detail = this.defaultItem;
      });
    },

    openDetail(item) {
      this.detail = Object.assign({}, item);
      this.details = true;
      console.log(this.detail);
    },

    deleteItem(item) {
      this.deleteMessage = Object.assign({}, item);
      this.dialogDelete = true;
    },

    close() {
      this.dialog = false;
      this.$nextTick(() => {
        this.deleteMessage = Object.assign({}, this.defaultItem);
      });
      this.getMessages();
    },

    closeDelete() {
      this.dialogDelete = false;
      this.$nextTick(() => {
        this.deleteMessage = Object.assign({}, this.defaultItem);
      });
      this.getMessages();
    },

    closeDeleteAll() {
      this.dialogDeleteAll = false;
      this.getMessages();
    },

    closeReadAll() {
      this.dialogReadAll = false;
      this.getMessages();
    },
  },
};
</script>

<style>
.center {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
}
.no-data {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}
</style>

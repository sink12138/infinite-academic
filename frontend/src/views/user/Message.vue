<template>
  <div>
    <v-card
      flat
      outlined
    >
      <v-card-text>
        <v-data-table
          :headers="headers"
          :items="messages"
          :options.sync="options"
          :server-items-length="totalMessages"
          :loading="loading"
          class="message"
        >
          <template v-slot:top>
            <v-toolbar flat>
              <v-toolbar-title>信息列表</v-toolbar-title>
              <v-divider
                inset
                vertical
              ></v-divider>
              <v-spacer></v-spacer>
              <v-checkbox
                v-model="isChooseRead"
                :label="'是否仅显示为已读消息'"
                @click="reloadRead()"
              ></v-checkbox>
              <v-checkbox
                v-model="isChooseNotRead"
                :label="'是否仅显示为未读消息'"
                @click="reloadNotRead()"
              ></v-checkbox>
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
            </v-toolbar>
          </template>
          <template v-slot:[`item.actions`]="{ item }">
            <v-icon
              small
              @click="deleteItem(item)"
            >
              mdi-delete
            </v-icon>
            <v-btn
              small
              @click="openDetail(item)"
            >
              <v-icon>mdi-information-outline</v-icon>
              详情
            </v-btn>
          </template>
          <template v-slot:no-data>
            <v-btn
              color="primary"
              @click="getMessages"
            >
              重置
            </v-btn>
          </template>
        </v-data-table>
      </v-card-text>
    </v-card>
    <v-dialog
      v-model="details"
      width="500"
    >
      <v-card class="text-left">
        <v-card-title
          class="headline grey lighten-2"
          primary-title
        >
          {{detail.title}}
        </v-card-title>
        <v-row
          no-gutters
        >
          <v-col cols="4">
            <v-card-text class="font-weight-black">
              消息时间：
            </v-card-text>
          </v-col>
          <v-col cols="8">
            <v-card-text>
              {{detail.time}}
            </v-card-text>
          </v-col>
        </v-row>
        <v-row no-gutters>
          <v-col cols="4">
            <v-card-text class="font-weight-black">
              正文：
            </v-card-text>
          </v-col>
          <v-col cols="8">
            <v-card-text>
              {{detail.content}}
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
          >
            关闭
          </v-btn>
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
  },
  data() {
    return {
      isChooseRead: false,
      isChooseNotRead: false,
      dialog: false,
      dialogDelete: false,
      dialogValid: false,
      totalNotReadMessages: 0,
      totalMessages: 0,
      options: {},
      loading: true,
      page: 0,
      read: false,
      size: 10,
      details: false,
      headers: [
        { text: "消息时间", value: "time", sortable: false },
        { text: "消息标题", value: "title", sortable: false },
        { text: "操作", value: "actions", sortable: false },
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
    };
  },
  watch: {
    dialog(val) {
      val || this.close();
    },
    dialogDelete(val) {
      val || this.closeDelete();
    },
    options: {
      handler() {
        this.getMessages();
      },
      deep: true,
    },
  },
  methods: {
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
      this.getMessages;
    },
    getMessages() {
      if (this.isChooseRead === false && this.isChooseNotRead === false) {
        this.loading = true;
        this.page = this.options.page;
        this.size =
          this.options.itemsPerPage <= 30 ? this.options.itemsPerPage : 30;
        console.log(this.page);
        console.log(this.size);
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
      } else if (this.isChooseRead === true) {
        this.loading = true;
        this.page = this.options.page;
        this.size =
          this.options.itemsPerPage <= 30 ? this.options.itemsPerPage : 30;
        console.log(this.page);
        console.log(this.size);
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
        this.page = this.options.page;
        this.size =
          this.options.itemsPerPage <= 30 ? this.options.itemsPerPage : 30;
        console.log(this.page);
        console.log(this.size);
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
  },
};
</script>

<style>
</style>

<template>
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
              v-model="read"
              :label="'是否仅显示为已读消息'"
              @change="getAccounts"
            ></v-checkbox>
            <v-checkbox
              :label="'是否仅显示为未读消息'"
              @change="getAccounts"
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
                    @click="closeDelete"
                  >否</v-btn>
                  <v-btn
                    color="blue darken-1"
                    text
                    @click="submitDelete"
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
          <v-btn color="primary" @click="details = true">详情</v-btn>
        </template>

        <template v-slot:no-data>
          <v-btn
            color="primary"
            @click="getAccounts"
          >
            重置
          </v-btn>
        </template>
      </v-data-table>
    </v-card-text>
  </v-card>
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
        this.totalMessages = response.data.data;
      }
    });
  },
  data() {
    return {
      dialog: false,
      dialogDelete: false,
      dialogValid: false,
      totalMessages: 0,
      options: {},
      loading: true,
      page: 0,
      read: false,
      size: 10,
      details:false,
      headers: [
        {
          text: "ID",
          align: "start",
          value: "id",
        },
        { text: "消息时间", value: "time", sortable: false },
        { text: "消息标题", value: "title", sortable: false },
        { text: "消息正文", value: "content", sortable: false },
        { text: "是否已读", value: "read", sortable: false },
        { text: "操作", value: "actions", sortable: false },
      ],
      messages: [],
      deleteMessage: {
        username: "",
        email: "",
        password: "",
        id: "",
      },
      defaultItem: {
        username: "",
        email: "",
        password: "",
        id: "",
      },
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
        this.getAccounts();
      },
      deep: true,
    },
  },
  methods: {
    submitDelete() {
      this.userName = "";
      this.passWords = "";
      this.dialogValid = true;
    },
    getAccounts() {
      this.loading = true;
      this.page = this.options.page;
      this.size =
        this.options.itemsPerPage <= 30 ? this.options.itemsPerPage : 30;
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
    },

    editItem(item) {
      this.deleteMessage = Object.assign({}, item);
      this.deleteMessage["password"] = "";
      this.dialog = true;
    },

    deleteItem(item) {
      this.deleteMessage = Object.assign({}, item);
      this.dialogDelete = true;
    },

    deleteItemConfirm() {
      this.$axios({
        method: "post",
        url: "api/account",
        params: {
          id: this.deleteMessage.id,
        },
      }).then((response) => {
        console.log(response.data);
        if (response.data.success === true) {
          this.$notify({
            title: "成功",
            message: "账号删除成功",
            type: "warning",
          });
        } else {
          this.$notify({
            title: "失败",
            message: "账号删除失败",
            type: "warning",
          });
        }
      });
      this.closeDelete();
    },

    close() {
      this.dialog = false;
      this.$nextTick(() => {
        this.deleteMessage = Object.assign({}, this.defaultItem);
      });
      this.getAccounts();
    },

    closeDelete() {
      this.dialogDelete = false;
      this.$nextTick(() => {
        this.deleteMessage = Object.assign({}, this.defaultItem);
      });
      this.getAccounts();
    },

    closeCheck() {
      this.dialogValid = false;
    },
  },
};
</script>

<style>
</style>

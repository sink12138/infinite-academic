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
      ><template v-slot:top>
          <v-toolbar flat>
            <v-toolbar-title>信息管理</v-toolbar-title>
          </v-toolbar>
          <v-divider
            inset
            vertical
          ></v-divider>
          <v-spacer></v-spacer>
          <v-checkbox
            v-model="read"
            :label="'是否仅显示未读信息'"
          ></v-checkbox>
          <v-dialog
            v-model="dialogDelete"
            max-width="500px"
          >
            <v-card>
              <v-card-title class="text-h5">真的要删除这条信息吗？</v-card-title>
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
                  @click="deleteMessage()"
                >是</v-btn>
                <v-spacer></v-spacer>
              </v-card-actions>
            </v-card>
          </v-dialog>
        </template>
        <template v-slot:[`message.actions`]="{message}">
          <v-icon
            small
            @click="openDeleteMessage(message)"
          >
            mdi-delete
          </v-icon>
        </template>
      </v-data-table>
    </v-card-text>
  </v-card>
</template>

<script>
export default {
  data() {
    return {
      read: false,
      page: 0,
      totalMessages: 0,
      size: 10,
      loading: true,
      options: {},
      headers: [
        {
          text: "编号",
          align: "start",
          value: 1,
        },
        { text: "信息标题", value: "title", sortable: false },
        { text: "信息时间", value: "time", sortable: false },
        { text: "操作", value: "actions", sortable: false },
      ],
      deleteList: [],
      dialogDelete: false,
      deleteMessages: {
        id: "",
        read: "",
        content: "",
        time: "",
        title: "",
      },
      defaultMessages: {
        id: "",
        read: "",
        content: "",
        time: "",
        title: "",
      },
      readList: [],
      messages: [],
    };
  },
  mounted() {
    this.$axios({
      method: "get",
      url: "/api/account/message/count",
    }).then((response) => {
      console.log(response.data);
      if (response.data.success === true) {
        this.totalMessages = response.data.data;
      }
    });
  },
  watch: {
    options: {
      handler() {
        this.getMessages();
      },
      deep: true,
    },
  },
  methods: {
    getMessages() {
      this.loading = true;
      this.page = this.options.pages;
      this.size =
        this.options.itemPerPage <= 30 ? this.options.itemPerPage : 30;
      this.$axios({
        methods: "get",
        url: "/api/account/message/list",
        params: {
          page: this.page - 1,
          read: this.read,
          size: this.size,
        }.then((response) => {
          console.log(response.data);
          if (response.data.success === true) {
            this.messages = response.data.data.messages;
            this.loading = false;
          }
        }),
      });
    },
    openDeleteMessage(message) {
      this.deleteMessages = Object.assign({}, message);
      this.dialogDelete = true;
    },
    deleteMessage() {
      this.$axios({
        methos: "post",
        url: "api/account/message/remove",
        params: {
          id: this.deleteMessage.id,
        },
      }).then((response) => {
        console.log(response.data);
        if (response.data.success === true) {
          this.$notify({
            title: "成功",
            message: "信息删除成功",
            type: "success",
          });
        }
      });
    },
    closeDelete() {
      this.dialogDelete = false;
      this.$nextTick(() => {
        this.deleteMessage = Object.assign({}, this.defaultMessage);
      });
      this.getMessages();
    },
  },
};
</script>

<style>
</style>

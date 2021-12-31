<template>
  <v-card
    flat
    outlined
  >
    <v-card-text>
      <v-data-table
        :headers="headers"
        :items="accounts"
        sort-by="id"
        :options.sync="options"
        :server-items-length="totalAccounts"
        :loading="loading"
        class="account"
      >
        <template v-slot:top>
          <v-toolbar
            flat
          >
            <v-toolbar-title>账户管理</v-toolbar-title>
            <v-divider
              inset
              vertical
            ></v-divider>
            <v-spacer></v-spacer>

            <v-text-field
              v-model="accountSearch"
              append-icon="mdi-magnify"
              @click:append="searchAccount"
              label="账号搜索"
              hide-details
              outlined
              @input="searchAccount"
            ></v-text-field>

            <v-spacer></v-spacer>

            <v-switch
              class="pr-2"
              v-model="scholar"
              @change="getAccounts"
              inset
              hide-details
              label="仅显示学者账户"
            ></v-switch>

            <v-dialog
              v-model="dialog"
              max-width="500px"
            >
              <template v-slot:activator="{ on, attrs }">
                <v-btn
                  outlined
                  v-bind="attrs"
                  v-on="on"
                >
                  <v-icon>
                    mdi-plus
                  </v-icon>
                  新增账户
                </v-btn>
              </template>
              <v-card>
                <v-card-title>
                  <span class="text-h5">{{ formTitle }}</span>
                </v-card-title>

                <v-card-text>
                  <v-container>
                    <v-row>
                      <v-col
                        cols="12"
                        sm="6"
                      >
                        <v-text-field
                          v-model="editedItem.username"
                          label="用户名"
                        ></v-text-field>
                      </v-col>
                      <v-col
                        cols="12"
                        sm="6"
                      >
                        <v-text-field
                          v-model="editedItem.password"
                          label="密码"
                        ></v-text-field>
                      </v-col>
                      <v-col
                        cols="12"
                        sm="6"
                      >
                        <v-text-field
                          v-model="editedItem.email"
                          label="注册邮箱"
                        ></v-text-field>
                      </v-col>
                      <v-col
                        cols="12"
                        sm="6"
                      >
                        <v-text-field
                          v-if="editedIndex != -1"
                          v-model="editedItem.id"
                          :disabled="true"
                          label="账号ID"
                        ></v-text-field>
                      </v-col>
                    </v-row>
                  </v-container>
                </v-card-text>

                <v-card-actions>
                  <v-spacer></v-spacer>
                  <v-btn
                    color="blue darken-1"
                    text
                    @click="close"
                  >
                    取消
                  </v-btn>
                  <v-btn
                    color="blue darken-1"
                    text
                    @click="check"
                  >
                    保存
                  </v-btn>
                </v-card-actions>
              </v-card>
            </v-dialog>

            <v-dialog v-model="dialogDelete" max-width="500px">
              <v-card>
                <v-card-title class="text-h5">真的要删除这个账号吗？</v-card-title>
                <v-card-actions>
                  <v-spacer></v-spacer>
                  <v-btn color="blue darken-1" text @click="closeDelete">再想想</v-btn>
                  <v-btn color="blue darken-1" text @click="check">确定</v-btn>
                  <v-spacer></v-spacer>
                </v-card-actions>
              </v-card>
            </v-dialog>

            <v-dialog v-model="dialogValid" max-width="500px">
              <v-card>
                <v-card-title class="text-h6">请验证管理员身份</v-card-title>
                  <v-card-text>
                    <v-container>
                      <v-row>
                        <v-col
                          cols="12"
                          sm="6"
                          md="4"
                        >
                          <v-text-field
                            v-model="userName"
                            label="管理员用户名"
                          ></v-text-field>
                        </v-col>
                        <v-col
                          cols="12"
                          sm="6"
                          md="4"
                        >
                          <v-text-field
                            v-model="passWords"
                            type="password"
                            label="密码"
                          ></v-text-field>
                        </v-col>
                      </v-row>
                    </v-container>
                  </v-card-text>

                  <v-card-actions>
                    <v-spacer></v-spacer>
                    <v-btn
                      color="blue darken-1"
                      text
                      @click="closeCheck"
                    >
                      取消
                    </v-btn>
                    <v-btn
                      color="blue darken-1"
                      text
                      @click="isValid"
                    >
                      验证
                    </v-btn>
                  </v-card-actions>
              </v-card>
            </v-dialog>

          </v-toolbar>
        </template>

        <template v-slot:[`item.actions`]="{ item }">
          <v-icon
            small
            class="mr-2"
            @click="editItem(item)"
          >
            mdi-pencil
          </v-icon>
          <v-icon
            small
            @click="deleteItem(item)"
          >
            mdi-delete
          </v-icon>
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
import { sha256 } from "js-sha256";

export default {
  data() {
    return {
      userName: "",
      passWords: "",
      accountSearch: "",
      searchEmail: "",
      searchName: "",
      dialog: false,
      dialogDelete: false,
      dialogValid: false,
      totalAccounts: 0,
      options: {},
      loading: true,
      page: 0,
      scholar: false,
      size: 10,
      sort: 'date-desc',
      headers: [
        {
          text: '用户名',
          align: 'start',
          value: 'username',
        },
        { text: '注册邮箱', value: 'email', sortable: false },
        { text: '注册时间', value: 'date' },
        { text: 'ID', value: 'id', sortable: false },
        { text: '研究人员ID', value: 'researcherId', sortable: false },
        { text: '操作', value: 'actions', sortable: false },
      ],
      accounts: [],
      editedIndex: -1,
      editedItem: {
        username: '',
        email: '',
        password: '',
        id: '',
      },
      defaultItem: {
        username: '',
        email: '',
        password: '',
        id: '',
      },
      refreshtime: 2,
    }
  },
  computed: {
      formTitle () {
        return this.editedIndex === -1 ? '新增账号' : '编辑账号'
      },
    },

    watch: {
      dialog (val) {
        val || this.close()
      },
      dialogDelete (val) {
        val || this.closeDelete()
      },
      options: {
        handler () {
          this.getAccounts()
        },
        deep: true,
      },
    },
  created() {
    
  },
  methods:{
    check () {
      this.userName = ""
      this.passWords = ""
      this.dialogValid = true
    },
    isValid() {
      this.$axios({
        method: "post",
        url: "api/admin/auth",
        params: {
          username: this.userName,
          password: sha256(this.passWords),
        },
      }).then((response) => {
        console.log(response.data);
        if (response.data.success === true) {
          this.$notify({
            title: "成功",
            message: "验证成功",
            type: "success",
          });
          this.dialogValid = false
          if (this.dialog === true) {
            this.save()
          }
          else {
            this.deleteItemConfirm()
          }
        } else {
          this.$notify({
            title: "失败",
            message: "验证失败",
            type: "warning",
          });
        }
      });
    },

    getAccounts () {
      this.loading = true
      this.page = this.options.page;
      this.size = this.options.itemsPerPage <= 30 ? this.options.itemsPerPage : 30;
      if (this.options.sortBy[0] == "username" || this.options.sortBy[0] == "date") {
        this.sort = this.options.sortBy[0];
        if (this.options.sortDesc[0] === true){
          this.sort = this.sort + "-desc"
        }
        else {
          this.sort = this.sort + "-asc"
        }
      }
      else {
        this.sort = "date-desc"
      }

      this.$axios({
        method: "get",
        url: "api/admin/users/query",
        params: {
          page: this.page - 1,
          scholar: this.scholar,
          size: this.size,
          sort: this.sort,
        },
      }).then((response) => {
        console.log(response.data);
        if (response.data.success === true) {
          this.accounts = response.data.data.items
          this.totalAccounts = response.data.data.pageCount * this.size
          this.loading = false
        } else {
          this.$notify({
            title: "失败",
            message: "账户信息获取失败",
            type: "warning",
          });
          this.loading = false
        }
      });
    },
    searchAccount () {
      this.loading = true
      this.page = this.options.page;
      this.size = this.options.itemsPerPage <= 30 ? this.options.itemsPerPage : 30;
      if (this.options.sortBy[0] == "username" || this.options.sortBy[0] == "date") {
        this.sort = this.options.sortBy[0];
        if (this.options.sortDesc[0] === true){
          this.sort = this.sort + "-desc"
        }
        else {
          this.sort = this.sort + "-asc"
        }
      }
      else {
        this.sort = "date-desc"
      }
      if (/^\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$/.test(this.accountSearch)) {
        this.searchEmail = this.accountSearch;
        this.searchName = "";
        this.$axios({
          method: "get",
          url: "api/admin/users/query",
          params: {
            page: this.page - 1,
            scholar: this.scholar,
            size: this.size,
            sort: this.sort,
            email: this.searchEmail,
          },
        }).then((response) => {
          console.log(response.data);
          if (response.data.success === true) {
            this.accounts = response.data.data.items
            this.totalAccounts = response.data.data.pageCount * this.size
            this.loading = false
          } else {
            this.$notify({
              title: "失败",
              message: "账户信息获取失败",
              type: "warning",
            });
            this.loading = false
          }
        });
      }
      else if (this.accountSearch == "") {
        this.getAccounts()
      }
      else {
        this.searchName = this.accountSearch;
        this.searchEmail = "";
        this.$axios({
          method: "get",
          url: "api/admin/users/query",
          params: {
            page: this.page - 1,
            scholar: this.scholar,
            size: this.size,
            sort: this.sort,
            username: this.searchName,
          },
        }).then((response) => {
          console.log(response.data);
          if (response.data.success === true) {
            this.accounts = response.data.data.items
            this.totalAccounts = response.data.data.pageCount * this.size
            this.loading = false
          } else {
            this.$notify({
              title: "失败",
              message: "账户信息获取失败",
              type: "warning",
            });
            this.loading = false
          }
        });
      }
    },

    editItem (item) {
      this.editedIndex = this.accounts.indexOf(item)
      this.editedItem = Object.assign({}, item)
      this.editedItem['password'] = ""
      this.dialog = true
    },

    deleteItem (item) {
      this.editedIndex = this.accounts.indexOf(item)
      this.editedItem = Object.assign({}, item)
      this.dialogDelete = true
    },

    deleteItemConfirm () {
      this.$axios({
        method: "post",
        url: "api/admin/users/remove",
        params: {
          id: this.editedItem.id,
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
      this.closeDelete()
    },

    close () {
      this.dialog = false
      this.$nextTick(() => {
        this.editedItem = Object.assign({}, this.defaultItem)
        this.editedIndex = -1
      })

      this.timer = setInterval(() => {
        this.refreshtime--;

        if (this.refreshtime === 0) {
          this.refreshtime = 2
          this.getAccounts();
          clearInterval(this.timer);
        }
      }, 1000);
    },

    closeDelete () {
      this.dialogDelete = false
      this.$nextTick(() => {
        this.editedItem = Object.assign({}, this.defaultItem)
        this.editedIndex = -1
      })
      
      this.timer = setInterval(() => {
        this.refreshtime--;

        if (this.refreshtime === 0) {
          this.refreshtime = 2
          this.getAccounts();
          clearInterval(this.timer);
        }
      }, 1000);
    },

    closeCheck () {
      this.dialogValid = false
    },

    save () {
      if (this.editedIndex > -1) {
        console.log(this.editedItem)
        if (this.editedItem.password.length != 0) {
          this.$axios({
            method: "post",
            url: "api/admin/users/save",
            params: {
              username: this.editedItem.username,
              password: sha256(this.editedItem.password),
              id: this.editedItem.id,
              email: this.editedItem.email,
            },
          }).then((response) => {
            console.log(response.data);
            if (response.data.success === true) {
              this.$notify({
                title: "成功",
                message: "账号修改成功",
                type: "warning",
              });
            } else {
              this.$notify({
                title: "失败",
                message: "账号修改失败",
                type: "warning",
              });
            }
          });
        }
        else {
          this.$axios({
            method: "post",
            url: "api/admin/users/save",
            params: {
              username: this.editedItem.username,
              id: this.editedItem.id,
              email: this.editedItem.email,
            },
          }).then((response) => {
            console.log(response.data);
            if (response.data.success === true) {
              this.$notify({
                title: "成功",
                message: "账号修改成功",
                type: "warning",
              });
            } else {
              this.$notify({
                title: "失败",
                message: "账号修改失败",
                type: "warning",
              });
            }
          });
        }
        
      } else {
        this.$axios({
          method: "post",
          url: "api/admin/users/save",
          params: {
            username: this.editedItem.username,
            password: sha256(this.editedItem.password),
            email: this.editedItem.email,
          },
        }).then((response) => {
          console.log(response.data);
          if (response.data.success === true) {
            this.$notify({
              title: "成功",
              message: "新增账号成功",
              type: "warning",
            });
          } else {
            this.$notify({
              title: "失败",
              message: "新增账号失败",
              type: "warning",
            });
          }
        });
      }
      this.close()
    },
  },
}
</script>

<style>
.addAccount {
  background:-webkit-gradient(linear, 100% 0%, 0% 0%,from(#59C2FD), to(#2B99FF));
  border-radius: 20px;
  color: #fff;
}
</style>
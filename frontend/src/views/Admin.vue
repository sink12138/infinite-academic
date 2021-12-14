<template>
  <div>
    <Banner 
      :title="{
        text: this.isLogin?this.menu[this.menu_id - 1].title:'管理员', 
        icon: 'mdi-account-cog',
        time: this.logoutTime
      }"
    ></Banner>

    <v-card
      v-if="this.isLogin"
      class="navigation"
      max-width="300"
      style="float:left"
    >
      <v-navigation-drawer
        permanent
        expand-on-hover
      >
        <v-list
          nav
          rounded
        >
          <v-list-item
            v-for="(item, id) in menu"
            :key="id"
            @click="setMenu(item.menu_id)"
          >
            <v-list-item-icon>
              <v-icon>{{ item.icon }}</v-icon>
            </v-list-item-icon>
            <v-list-item-title>{{ item.title }}</v-list-item-title>
          </v-list-item>
        </v-list>
      </v-navigation-drawer>
    </v-card>



    <v-container>
      <v-card
        class="welcome"
        v-if="menu_id == 0"
        flat
        outlined
      >
        <v-card-title>
          <v-row 
            justify="center" 
            align="center"
          >
            <h1>欢迎来到管理员界面！</h1>
          </v-row>
        </v-card-title>
          
        <v-card-text>
          <v-form
            ref="userForm"
            v-model="valid"
            lazy-validation
          >
            <v-row 
              justify="center" 
              align="center"
            >
              <v-col 
                clos="12" 
                sm="4"
              >
                <v-text-field
                  v-model="userName"
                  :counter="10"
                  :rules="userNameRules"
                  label="用户名"
                  required
                ></v-text-field>
              </v-col>
            </v-row>

            <v-row 
              justify="center" 
              align="center"
            >
              <v-col 
                clos="12" 
                sm="4"
              >
                <v-text-field
                  v-model="passWords"
                  :rules="passWordsRules"
                  label="密码"
                  type="password"
                  required
                ></v-text-field>
              </v-col>
            </v-row>

            <v-row>
              <v-col>
                <v-btn
                  v-if="!isLogin"
                  color="light-blue lighten-4"
                  @click="Login"
                >
                  <v-icon left>
                    mdi-account-cog
                  </v-icon>
                  登录管理员账户
                </v-btn>
              </v-col>
            </v-row>
          </v-form>
        </v-card-text>
      </v-card>

      <v-card
        class="personal"
        v-if="menu_id == 1"
        flat
        outlined
      >
        <v-card-title>
          <v-row 
            justify="center" 
            align="center"
          >
            <h1>欢迎进入，{{ this.userOldName }}！</h1>
          </v-row>
        </v-card-title>

        <v-card-text>
          <v-form
            ref="userModifyForm"
            v-model="valid"
            lazy-validation
          >
            <v-row 
              justify="center" 
              align="center"
            >
              <v-col 
                clos="12" 
                sm="4"
              >
                <v-text-field
                  v-model="userName"
                  :counter="10"
                  :rules="userNameRules"
                  label="用户名"
                  required
                ></v-text-field>
              </v-col>
            </v-row>

            <v-row 
              justify="center" 
              align="center"
            >
              <v-col 
                clos="12" 
                sm="4"
              >
                <v-text-field
                  v-model="passWords"
                  :rules="passWordsRules"
                  label="密码"
                  type="password"
                  required
                ></v-text-field>
              </v-col>
            </v-row>

            <v-row>
              <v-col 
                clos="12" 
                sm="4"
              ></v-col>
              <v-col 
                clos="12" 
                sm="2"
              >
                <v-btn
                  :disabled="!valid"
                  color="success"
                  @click="modify"
                >
                  修改
                </v-btn>
              </v-col>

              <v-col 
                clos="12" 
                sm="2"
              >
                <v-btn
                  color="primary"
                  @click="Logout"
                >
                  登出
                </v-btn>
              </v-col>
              <v-col 
                clos="12" 
                sm="4"
              ></v-col>
            </v-row>
          </v-form>
        </v-card-text>
      </v-card>

      <v-card
        class="accountmanagement"
        v-if="menu_id == 2"
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
                  single-line
                  hide-details
                ></v-text-field>

                <v-checkbox
                  v-model="scholar"
                  :label="'是否仅显示学者账户'"
                ></v-checkbox>

                <v-dialog
                  v-model="dialog"
                  max-width="500px"
                >
                  <template v-slot:activator="{ on, attrs }">
                    <v-btn
                      color="primary"
                      dark
                      v-bind="attrs"
                      v-on="on"
                    >
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
                            md="4"
                          >
                            <v-text-field
                              v-model="editedItem.username"
                              label="用户名"
                            ></v-text-field>
                          </v-col>
                          <v-col
                            cols="12"
                            sm="6"
                            md="4"
                          >
                            <v-text-field
                              v-model="editedItem.password"
                              label="密码"
                            ></v-text-field>
                          </v-col>
                          <v-col
                            cols="12"
                            sm="6"
                            md="4"
                          >
                            <v-text-field
                              v-model="editedItem.email"
                              label="注册邮箱"
                            ></v-text-field>
                          </v-col>
                          <v-col
                            cols="12"
                            sm="6"
                            md="4"
                          >
                            <v-text-field
                              v-if="editedIndex != -1"
                              v-model="editedItem.id"
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
                        @click="save"
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
                      <v-btn color="blue darken-1" text @click="deleteItemConfirm">确定</v-btn>
                      <v-spacer></v-spacer>
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

      <v-card
        v-if="menu_id == 3"
        flat
      >
        <p>欢迎来到数据管理界面！</p>
      </v-card>
      <v-card
        v-if="menu_id == 4"
        flat
      >
        <p>欢迎来到数据更新界面！</p>
      </v-card>
      <v-card
        v-if="menu_id == 5"
        flat
      >
        <p>欢迎来到账号审核界面！</p>
      </v-card>
      <v-card
        v-if="menu_id == 6"
        flat
      >
        <p>欢迎来到数据审核界面！</p>
      </v-card>
      <v-card
        v-if="menu_id == 7"
        flat
      >
        <p>欢迎来到系统设置界面！</p>
      </v-card>
    </v-container>
  

  </div>
</template>

<script>
import Banner from '../components/AdminBanner.vue'
import { sha256 } from "js-sha256";

export default {
  components: {
    Banner,
  },
  data() {
    return {
      isLogin: false,
      logoutTime: "",
      valid: true,
      userOldName: "管理员",
      userName: "admin",
      userNameRules: [
        v => !!v || "用户名不能为空",
        v => (v && v.length <= 10) || "用户名不能超过10个字符",
      ],
      passWords: "admin-academic-2021",
      passWordsRules: [
        v => !!v || "密码不能为空",
      ],
      accountSearch: "",
      searchEmail: "",
      searchName: "",
      menu_id: 0,
      menu:[
        {
          menu_id: 1,
          title:"个人中心",
          icon:"mdi-account-cog-outline",
        },
        {
          menu_id: 2,
          title:"账号管理",
          icon:"mdi-account-box-multiple-outline",
        },
        {
          menu_id: 3,
          title:"数据管理",
          icon:"mdi-folder",
        },
        {
          menu_id: 4,
          title:"数据更新",
          icon:"mdi-folder",
        },
        {
          menu_id: 5,
          title:"账号审核",
          icon:"mdi-folder",
        },
        {
          menu_id: 6,
          title:"数据审核",
          icon:"mdi-folder",
        },
        {
          menu_id: 7,
          title:"系统设置",
          icon:"mdi-folder",
        },
      ],
      dialog: false,
      dialogDelete: false,
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
    setMenu(i) {
      this.menu_id = i;
    },
    Login() {
      let token = window.localStorage.token;
      this.$axios({
        method: "post",
        url: "api/admin/login",
        params: {
          username: this.userName,
          password: sha256(this.passWords),
        },
        headers:{
          'token':token
        }
      }).then((response) => {
        console.log(response.data);
        if (response.data.success === true) {
          this.$notify({
            title: "成功",
            message: "登陆成功",
            type: "success",
          });
          this.isLogin = true;
          this.menu_id = 1;
          this.userOldName = this.userName;
          this.logoutTime = new Date((new Date().getTime() + 1000 * 30 * 60)).toLocaleTimeString();
          this.leftTime = 30 * 60;
          this.timer = setInterval(() => {
            this.leftTime--;

            if (this.leftTime === 0) {
              this.Logout();
              clearInterval(this.timer);
            }
          }, 1000);
        } else {
          this.$notify({
            title: "失败",
            message: "登录名或密码错误",
            type: "warning",
          });
        }
      });
    },
    Logout() {
      let token = window.localStorage.token;
      this.$axios({
        method: "post",
        url: "api/admin/logout",
        headers:{
          'token':token
        }
      }).then((response) => {
        console.log(response.data);
        if (response.data.success === true) {
          this.$notify({
            title: "成功",
            message: "登出成功",
            type: "success",
          });
          this.isLogin = false;
          this.menu_id = 0;
          this.userOldName = "管理员";
        } else {
          this.$notify({
            title: "失败",
            message: "登出失败",
            type: "warning",
          });
        }
      });
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
        } else {
          this.$notify({
            title: "失败",
            message: "登出失败",
            type: "warning",
          });
        }
      });
    },
    modify() {
      this.userOldName = this.userName;
      this.$notify({
            title: "修改成功",
            message: "修改成功！",
            type: "success",
      });
    },
    test () {
      this.$notify({
            title: "test",
            message: "test！",
            type: "success",
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
      this.getAccounts()
      this.getAccounts()
    },

    closeDelete () {
      this.dialogDelete = false
      this.$nextTick(() => {
        this.editedItem = Object.assign({}, this.defaultItem)
        this.editedIndex = -1
      })
      this.getAccounts()
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

</style>
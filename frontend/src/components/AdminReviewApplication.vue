<template>
  <v-card
    width="4000"
    flat
    outlined
  >
    <v-card-text>
      <v-data-table
        :headers="headers"
        :items="applications"
        :options.sync="options"
        :server-items-length="totalApplications"
        :loading="loading"
        :calculate-widths="true"
        class="applications"
      >
        <template v-slot:top>
          <v-toolbar
            flat
          >
            <v-toolbar-title>审核申请</v-toolbar-title>
            <v-divider
              inset
              vertical
            ></v-divider>
            <v-spacer></v-spacer>
            
            <v-radio-group
              v-model="radioGroup"
              row
              @change="getApplications"
            >
              <v-radio
                v-for="(item, id) in radio"
                :key="id"
                :label="item.title"
                :value="item.num"
              ></v-radio>
            </v-radio-group>
            
            <v-divider
              inset
              vertical
            ></v-divider>
            
            <v-col 
              cols="12"
              sm="3"
            >
              <v-select
                v-model="select"
                :items="items"
                item-text="text"
                item-value="num"
                single-line
                return-object
                dense
                @change="getApplications"
              ></v-select>
            </v-col>
            
            <v-dialog
              v-model="dialog"
              max-width="500px"
            >
              <v-card>
                <v-card-title>
                  <span class="text-h5">申请详情</span>
                </v-card-title>

                <v-card-text>
                  <v-container>
                    <v-text-field
                      v-model="content"
                    ></v-text-field>
                  </v-container>
                </v-card-text>

                <v-card-actions>
                  <v-spacer></v-spacer>
                  <v-btn
                    color="blue darken-1"
                    text
                    @click="close"
                  >
                    关闭
                  </v-btn>
                </v-card-actions>
              </v-card>
            </v-dialog>

            <v-dialog v-model="dialogFail" max-width="500px">
              <v-card>
                <v-card-title class="text-h5">真的要不通过这个申请吗？</v-card-title>
                <v-card-actions>
                  <v-spacer></v-spacer>
                  <v-btn color="blue darken-1" text @click="closeFail">再想想</v-btn>
                  <v-btn color="blue darken-1" text @click="fail">确定</v-btn>
                  <v-spacer></v-spacer>
                </v-card-actions>
              </v-card>
            </v-dialog>
          </v-toolbar>
        </template>

        <template v-slot:[`item.actions`]="{ item }">
          <v-icon
            @click="checkItem(item)"
          >
            mdi-eye-settings-outline
          </v-icon>
          <v-icon
            @click="passItem(item)"
          >
            mdi-check-circle-outline
          </v-icon>
          <v-icon
            @click="failItem(item)"
          >
            mdi-close-circle-outline
          </v-icon>
        </template>

        <template v-slot:no-data>
          <v-btn
            color="primary"
            @click="refresh"
          >
            重置
          </v-btn>
        </template>
      </v-data-table>
    </v-card-text>

    <v-btn
      color="primary"
      @click="test"
    >
      test
    </v-btn>
  </v-card>
</template>

<script>
export default {
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
          text: '申请ID',
          align: 'start',
          value: 'id',
          sortable: false,
        },
        { text: '申请类型', value: 'type', sortable: false },
        { text: '申请人ID', value: 'userId', sortable: false },
        { text: '申请时间', value: 'time', sortable: false },
        { text: '邮箱', value: 'email', sortable: false },
        { text: 'websiteLink', value: 'websiteLink', sortable: false },
        { text: '文件Token', value: 'fileToken', sortable: false },
        { text: '当前状态', value: 'status', sortable: false },
        { text: '操作', value: 'actions', sortable: false },
      ],
      applications: [
        {
          id: 0,
          type: 'test',
          userId: 2,
          time: '2021.12.15',
          email: 'test',
          websiteLink: 'test',
          fileToken: 'test',
          status: 'test',
        }
      ],
      content: '',
      radioGroup: 0,
      radio: [
        {
          num: 0,
          title: '全部'
        },
        {
          num: 1,
          title: '审核中'
        },
        {
          num: 2,
          title: '审核不通过'
        },
        {
          num: 3,
          title: '审核通过'
        }
      ],
      select: { text: '全部', num: '0' },
      items: [
        { text: '全部', num: '0' },
        { text: '学者认证', num: '1' },
        { text: '门户认领', num: '2' },
        { text: '门户信息修改', num: '3' },
        { text: '添加论文', num: '4' },
        { text: '下架论文', num: '5' },
        { text: '修改论文信息', num: '6' },
        { text: '专利转让', num: '7' },
        
      ],
    }
  },
  computed: {
      
  },

  watch: {
    options: {
      handler () {
        this.getApplications()
      },
      deep: true,
    },
  },

  created () {
    
  },

  methods:{
    getApplications () {
      this.loading = true
      this.page = this.options.page;
      this.size = this.options.itemsPerPage <= 30 ? this.options.itemsPerPage : 30;

      if (this.radioGroup == 0 && this.select['num'] == 0) {
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
            this.applications = response.data.data.items
            this.totalApplications = response.data.data.pageCount * this.size
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
      else if (this.radioGroup == 0 && this.select['num'] != 0) {
        this.$axios({
          method: "get",
          url: "api/admin/review/list",
          params: {
            page: this.page - 1,
            size: this.size,
            type: this.select['text']
          },
        }).then((response) => {
          console.log(response.data);
          if (response.data.success === true) {
            this.applications = response.data.data.items
            this.totalApplications = response.data.data.pageCount * this.size
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
      else if (this.radioGroup != 0 && this.select['num'] == 0) {
        this.$axios({
          method: "get",
          url: "api/admin/review/list",
          params: {
            page: this.page - 1,
            size: this.size,
            status: this.radio[this.radioGroup]['title']
          },
        }).then((response) => {
          console.log(response.data);
          if (response.data.success === true) {
            this.applications = response.data.data.items
            this.totalApplications = response.data.data.pageCount * this.size
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
      else {
        this.$axios({
          method: "get",
          url: "api/admin/review/list",
          params: {
            page: this.page - 1,
            size: this.size,
            type: this.select['text'],
            status: this.radio[this.radioGroup]['title']
          },
        }).then((response) => {
          console.log(response.data);
          if (response.data.success === true) {
            this.applications = response.data.data.items
            this.totalApplications = response.data.data.pageCount * this.size
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

    checkItem (item) {
      this.checkItemIndex = this.applications.indexOf(item)

      this.$axios({
        method: "get",
        url: "api/admin/review/details/" + item.id,
        params: {
          id: item.id,
        },
      }).then((response) => {
        console.log(response.data);
        if (response.data.success === true) {
          this.content = response.data.data.content
        } else {
          this.$notify({
            title: "失败",
            message: "账户信息获取失败",
            type: "warning",
          });
        }
      });
      this.dialog = true
    },

    passItem (item) {
      this.checkItemIndex = this.applications.indexOf(item)

      console.log(item)

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
      }
      else if (item.type === "门户认领") {
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
      }
      else if (item.type === "门户修改") {
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
      }
      else if (item.type === "添加论文") {
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
      }
      else if (item.type === "修改论文") {
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
      }
      else if (item.type === "移除论文") {
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
      }
      else if (item.type === "专利转让") {
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
    },

    failItem () {
      this.dialogFail = true
    },

    fail (item) {
      this.checkItemIndex = this.applications.indexOf(item)

      this.$axios({
        method: "post",
        url: "api/admin/review/reject",
        params: {
          id: item.id,
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
      this.closeFail()
    },

    close () {
      this.dialog = false
      this.content = ''

      this.getApplications()
      this.getApplications()
    },

    closeFail () {
      this.dialogFail = false
      this.getApplications()
    },

    test () {
      this.$axios({
        method: "post",
        url: "/api/scholar/certify",
        params: {
          ctfApp:{
            content:{
              claim:{
                portals: [],
              },
              code: 'test',
              create:{
                currentInst:{
                  id:'test',
                  name:'test'
                },
                gIndex:'test',
                hIndex:'test',
                institutions:'test',
                interests:'test',
                name:'test',
              }
            },
            email:'test',
            fileToken:'test',
            websiteLink:'test'
          }
        }
      }).then(response => {
        console.log(response.data)
        this.snackbarSub=true
      }).catch(error => {
        console.log(error)
      })
    }

  },
}
</script>

<style>

</style>
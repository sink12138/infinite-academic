<template>
  <v-card
    width="4000"
    flat
    outlined
  >
    <v-card-text>
      <v-data-table
        :headers="headers"
        :items="tasks"
        :loading="loading"
        class="task"
      >
        <template v-slot:top>
          <v-toolbar
            flat
          >
            <v-toolbar-title>定时任务管理</v-toolbar-title>
            <v-divider
              inset
              vertical
            ></v-divider>
            <v-spacer></v-spacer>

            <v-dialog
              v-model="dialog"
              max-width="500px"
            >
              <v-card>
                <v-card-title>
                  <span class="text-h5">执行频率设置</span>
                </v-card-title>

                <v-card-text>
                  <v-container>
                    <v-slider
                      v-if="radioGroup == 0"
                      v-model="frequency"
                      step="1"
                      label="频率"
                      max="31"
                      min="1"
                      thumb-label="always"
                      ticks
                    ></v-slider>
                    <v-slider
                      v-if="radioGroup == 1"
                      v-model="frequency"
                      step="1"
                      label="频率"
                      max="12"
                      min="1"
                      thumb-label="always"
                      ticks
                    ></v-slider>

                    <v-radio-group v-model="radioGroup">
                      <v-radio
                        v-for="(item, id) in radio"
                        :key="id"
                        :label="item.title"
                        :value="item.num"
                      ></v-radio>
                    </v-radio-group>

                    <v-slider
                      v-model="hour"
                      step="1"
                      label="时间"
                      max="23"
                      min="0"
                      thumb-label="always"
                      ticks
                    ></v-slider>

                    <p
                      v-if="radioGroup == 0"
                    >
                      频率：每{{ frequency }}天{{ hour }}点更新
                    </p>
                    <p
                      v-if="radioGroup == 1"
                    >
                      频率：每{{ frequency }}月{{ hour }}点更新
                    </p>
                    <p>{{ cron }}</p>
                  </v-container>
                </v-card-text>

                <v-card-actions>
                  <v-spacer></v-spacer>
                  <v-btn
                    color="blue darken-1"
                    @click="getCron"
                  >
                    cron
                  </v-btn>

                  <v-btn
                    color="blue darken-1"
                    :disabled="radioGroup < 0"
                    @click="set"
                  >
                    确定
                  </v-btn>

                  <v-btn
                    color="blue darken-1"
                    @click="close"
                  >
                    关闭
                  </v-btn>
                </v-card-actions>
              </v-card>
            </v-dialog>
          </v-toolbar>
        </template>

        <template v-slot:[`item.actions`]="{ item }">
          <v-icon
            @click="setItem(item)"
          >
            mdi-cogs
          </v-icon>
          <v-icon
            @click="runItem(item)"
          >
            mdi-play-circle-outline
          </v-icon>
          <v-icon
            @click="stopItem(item)"
          >
            mdi-pause-circle-outline
          </v-icon>
        </template>

        <template v-slot:no-data>
          <v-btn
            color="primary"
            @click="getTasks"
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
  data() {
    return {
      dialog: true,
      loading: true,
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
          title: '天'
        },
        {
          num: 1,
          title: '月'
        }
      ],
      frequency: 0,
      hour: 0,
      cron: '',
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
      
    },

    failItem () {

    },

    fail (item) {
      this.checkItemIndex = this.applications.indexOf(item)

    },

    close () {
      console.log(this.radioGroup)
      this.dialog = false

    },

    closeFail () {
      this.dialogFail = false
      this.getApplications()
    },

    getCron () {
      if (this.radioGroup == 0) {
        this.cron = '0 0 ' + this.hour + ' ' + this.frequency +' * ?'
      }
      else {
        this.cron = '0 0 ' + this.hour + ' * ' + this.frequency +' ?'
      }
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
<template>
  <v-dialog v-model="dialog" width="500">
    <template v-slot:activator="{ on, attrs }">
      <v-icon
        v-if="path == 'admin'"
        v-bind="attrs"
        v-on="on"
        @click="getDetails()"
        >mdi-information-outline</v-icon
      >
      <v-btn
        v-else
        outlined
        v-bind="attrs"
        v-on="on"
        small
        @click="getDetails()"
      >
        <v-icon>mdi-information-outline</v-icon>
        详细
      </v-btn>
    </template>

    <v-card class="text-left">
      <v-card-title class="text-h5 grey lighten-2">
        {{ message.type }}
      </v-card-title>

      <!-- 学者认证 -->
      <v-container class="pa-0" v-if="this.type == 'certification'">
        <v-row no-gutters>
          <v-col cols="4">
            <v-card-text class="font-weight-black"> 门户管理: </v-card-text>
          </v-col>
          <v-col cols="8">
            <v-card-text v-if="this.content.create != null">
              新建门户
              <v-btn icon @click="reveal1 = true">
                <v-icon> mdi-dots-horizontal-circle-outline </v-icon>
              </v-btn>
            </v-card-text>
            <v-card-text v-else>
              认领已有门户
              <v-btn icon @click="reveal1 = true">
                <v-icon> mdi-dots-horizontal-circle-outline </v-icon>
              </v-btn>
            </v-card-text>
          </v-col>
        </v-row>
      </v-container>

      <!-- 学者认证 -->
      <v-expand-transition>
        <v-card
          v-if="reveal1"
          class="transition-fast-in-fast-out v-card--reveal"
          style="height: 100%"
        >
          <v-card-text class="text-h5 text--primary grey lighten-2">
            门户详细信息
          </v-card-text>
          <v-row no-gutters>
            <v-col cols="4">
              <v-card-text class="font-weight-black"> 门户管理: </v-card-text>
            </v-col>
            <v-col cols="8">
              <v-card-text v-if="this.content.create != null">
                新建门户
              </v-card-text>
              <v-card-text v-else> 认领已有门户 </v-card-text>
            </v-col>
          </v-row>
          <v-container class="pa-0" v-if="this.content.create != null">
            <v-row no-gutters>
              <v-col cols="4">
                <v-card-text class="font-weight-black"> 学者姓名: </v-card-text>
              </v-col>
              <v-col cols="8">
                <v-card-text>
                  {{ this.content.create.name }}
                </v-card-text>
              </v-col>
            </v-row>
            <v-row no-gutters>
              <v-col cols="4">
                <v-card-text class="font-weight-black"> 机构: </v-card-text>
              </v-col>
              <v-col cols="8">
                <v-card-text
                  v-if="this.content.create.currentInst.id != null"
                  class="link"
                  @click="
                    href('institution', this.content.create.currentInst.id)
                  "
                >
                  {{ this.content.create.currentInst.name }}
                </v-card-text>
                <v-card-text v-else>
                  {{ this.content.create.currentInst.name }}
                </v-card-text>
              </v-col>
            </v-row>
            <v-row no-gutters v-if="this.content.create.interests.length != 0">
              <v-col cols="4">
                <v-card-text class="font-weight-black"> 研究兴趣: </v-card-text>
              </v-col>
              <v-col cols="8">
                <v-card-text>
                  <span
                    v-for="interest in this.content.create.interests"
                    :key="interest"
                  >
                    {{ interest }}&nbsp;
                  </span>
                </v-card-text>
              </v-col>
            </v-row>
          </v-container>
          <v-container class="pa-0" v-else-if="this.content.claim != null">
            <v-row no-gutters>
              <v-col cols="4">
                <v-card-text class="font-weight-black"> 学者姓名: </v-card-text>
              </v-col>
              <v-col cols="8">
                <v-card-text>
                  {{ this.content.create.name }}
                </v-card-text>
              </v-col>
            </v-row>
          </v-container>
          <v-container class="text-center" v-else>
            <v-progress-circular size="80" indeterminate></v-progress-circular>
          </v-container>

          <v-divider></v-divider>
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn outlined @click="reveal1 = false">
              <v-icon>mdi-reply</v-icon>
              退出
            </v-btn>
            <v-spacer></v-spacer>
          </v-card-actions>
        </v-card>
      </v-expand-transition>

      <!-- 门户认领 -->
      <v-container class="pa-0" v-if="this.type == 'claim'">
        <v-row no-gutters>
          <v-col cols="4">
            <v-card-text class="font-weight-black"> 申请认领门户: </v-card-text>
          </v-col>
          <v-col cols="8">
            <v-card-text>
              <span
                class="link"
                v-for="item in this.data"
                :key="item.id"
                @click="href('author', item.id)"
              >
                {{ item.name }}&nbsp;
              </span>
            </v-card-text>
          </v-col>
        </v-row>
      </v-container>

      <!-- 修改门户 -->
      <v-container class="pa-0" v-if="this.type == 'modification'">
        <v-row no-gutters>
          <v-col cols="4">
            <v-card-text class="font-weight-black"> 修改原因: </v-card-text>
          </v-col>
          <v-col cols="8">
            <v-card-text>
              {{ this.content.description }}
            </v-card-text>
          </v-col>
        </v-row>
      </v-container>

      <!-- 添加论文 -->
      <v-container class="pa-0" v-if="this.type == 'new_paper'">
        <v-row no-gutters>
          <v-col cols="4">
            <v-card-text class="font-weight-black"> 论文标题: </v-card-text>
          </v-col>
          <v-col cols="8">
            <v-card-text>
              {{ this.content.add.title }}
              <v-btn icon @click="reveal2 = true">
                <v-icon> mdi-dots-horizontal-circle-outline </v-icon>
              </v-btn>
            </v-card-text>
          </v-col>
        </v-row>
      </v-container>

      <!-- 修改论文 -->
      <v-container class="pa-0" v-if="this.type == 'edit_paper'">
        <v-row no-gutters>
          <v-col cols="4">
            <v-card-text class="font-weight-black"> 论文标题:</v-card-text>
          </v-col>
          <v-col cols="8">
            <v-card-text>
              {{ this.content.edit.title }}
              <v-btn icon @click="reveal2 = true">
                <v-icon> mdi-dots-horizontal-circle-outline </v-icon>
              </v-btn>
            </v-card-text>
          </v-col>
        </v-row>
      </v-container>

      <!-- 移除论文 -->
      <v-container class="pa-0" v-if="this.type == 'remove_paper'">
        <v-row no-gutters>
          <v-col cols="4">
            <v-card-text class="font-weight-black"> 论文标题: </v-card-text>
          </v-col>
          <v-col cols="8">
            <v-card-text v-html="getTitle(this.content.paperId)">
            </v-card-text>
          </v-col>
        </v-row>
      </v-container>

      <v-expand-transition>
        <v-card
          v-if="reveal2"
          class="transition-fast-in-fast-out v-card--reveal"
          style="height: 100%"
        >
          <v-card-text class="text-h5 text--primary grey lighten-2">
            论文详细信息
          </v-card-text>

          <CardPaper 
            v-if="this.type == 'new_paper'"
            :item="content.add" elevation="0"
          ></CardPaper>
          <CardPaper
            v-if="this.type == 'edit_paper'"
            :item="content.edit" elevation="0"
          ></CardPaper>
          <v-divider></v-divider>
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn outlined @click="reveal2 = false">
              <v-icon>mdi-reply</v-icon>
              退出
            </v-btn>
            <v-spacer></v-spacer>
          </v-card-actions>
        </v-card>
      </v-expand-transition>

      <!-- 专利转让 -->
      <v-container class="pa-0" v-if="this.type == 'transfer'">
        <v-row no-gutters>
          <v-col cols="4">
            <v-card-text class="font-weight-black">地址:</v-card-text>
          </v-col>
          <v-col cols="8">
            <v-card-text>{{ this.content.address }}</v-card-text>
          </v-col>
        </v-row>
        <v-row no-gutters>
          <v-col cols="4">
            <v-card-text class="font-weight-black">代理机构:</v-card-text>
          </v-col>
          <v-col cols="8">
            <v-card-text>{{ this.content.agency }}</v-card-text>
          </v-col>
        </v-row>
        <v-row no-gutters>
          <v-col cols="4">
            <v-card-text class="font-weight-black">代理人:</v-card-text>
          </v-col>
          <v-col cols="8">
            <v-card-text>{{ this.content.agent }}</v-card-text>
          </v-col>
        </v-row>
        <v-row no-gutters>
          <v-col cols="4">
            <v-card-text class="font-weight-black">申请人:</v-card-text>
          </v-col>
          <v-col cols="8">
            <v-card-text>{{ this.content.applicant }}</v-card-text>
          </v-col>
        </v-row>
      </v-container>

      <!-- 申请时间 -->
      <v-row no-gutters>
        <v-col cols="4">
          <v-card-text class="font-weight-black"> 申请时间: </v-card-text>
        </v-col>
        <v-col cols="8">
          <v-card-text>
            {{ message.time }}
          </v-card-text>
        </v-col>
      </v-row>

      <!-- 申请状态 -->
      <v-row no-gutters>
        <v-col cols="4">
          <v-card-text class="font-weight-black"> 申请状态: </v-card-text>
        </v-col>
        <v-col cols="8">
          <v-card-text>
            {{ message.status }}
          </v-card-text>
        </v-col>
      </v-row>

      <!-- 联系邮箱 -->
      <v-row no-gutters>
        <v-col cols="4">
          <v-card-text class="font-weight-black"> 联系邮箱: </v-card-text>
        </v-col>
        <v-col cols="8">
          <v-card-text>
            {{ message.email }}
          </v-card-text>
        </v-col>
      </v-row>

      <v-divider></v-divider>

      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="primary" text @click="dialog = false"> 关闭 </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>
import CardPaper from "../components/card/CardPaper.vue";
export default {
  components: {
    CardPaper,
  },
  data() {
    return {
      dialog: false,
      reveal1: false,
      reveal2: false,
      type: "",
      data: [],
      basic: {},
      content: {},
    };
  },
  props: {
    message: {
      type: Object,
      default: () => {},
    },
    path: {
      type: String,
    },
  },
  methods: {
    getDetails() {
      switch (this.message.type) {
        case "学者认证":
          this.type = "certification";
          break;
        case "门户认领":
          this.type = "claim";
          break;
        case "门户修改":
          this.type = "modification";
          break;
        case "添加论文":
          this.type = "new_paper";
          break;
        case "修改论文":
          this.type = "edit_paper";
          break;
        case "移除论文":
          this.type = "remove_paper";
          break;
        case "专利转让":
          this.type = "transfer";
          break;
        default:
          this.type = "";
      }
      console.log(this.$route.path);
      if (this.$route.path == "/user/apply") {
        this.$axios({
          method: "get",
          url: "/api/account/application/details/" + this.message.id,
        })
          .then((res) => {
            if (res.data.success) {
              this.basic = res.data.data.basic;
              this.content = res.data.data.content;
            } else {
              console.log(res.data.message);
            }
          })
          .catch((error) => {
            console.log(error);
          });
      } else if (this.$route.path == "/admin") {
        this.$axios({
          method: "get",
          url: "/api/admin/review/details/" + this.message.id,
        })
          .then((res) => {
            if (res.data.success) {
              this.basic = res.data.data.basic;
              this.content = res.data.data.content;
            } else {
              console.log(res.data.message);
            }
          })
          .catch((error) => {
            console.log(error);
          });
      }
    },
    getBrief(id) {
      this.$axios({
        method: "post",
        url: "/api/search/info/brief",
        data: {
          entity: "paper",
          ids: [id],
        },
      })
        .then((res) => {
          if (res.data.success) {
            this.data = res.data.data;
          } else {
            console.log(res.data.message);
          }
        })
        .catch((error) => {
          console.log(error);
        });
    },
    getTitle(id) {
      this.$axios({
        method: "post",
        url: "/api/search/info/brief",
        data: {
          entity: "paper",
          ids: [id],
        },
      })
        .then((res) => {
          if (res.data.success) {
            return res.data.data[0].name;
          } else {
            console.log(res.data.message);
          }
        })
        .catch((error) => {
          console.log(error);
        });
    },
    href(type, id) {
      if (id == null) {
        this.$notify({
          title: "数据缺失",
          message: "信息暂未收录，给您带来不便敬请谅解。",
          type: "warning",
        });
        return;
      }
      let { href } = this.$router.resolve({ path: type, query: { id: id } });
      window.open(href, "_blank");
    },
  },
};
</script>

<style scoped>
.v-card--reveal {
  bottom: 0;
  opacity: 1 !important;
  position: absolute !important;
  height: 100%;
  width: 100%;
}
</style>
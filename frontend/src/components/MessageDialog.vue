<template>
  <v-dialog
    v-model="dialog"
    width="500"
  >
    <template v-slot:activator="{ on, attrs }">
      <v-btn
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
        {{message.title}}
      </v-card-title>

      <div v-if="this.type == 'certification'">
        <div 
          v-if="this.content.claim != null"
          class="d-inline-flex"
        >
          <v-card-text class="font-weight-black">
            申请认领门户ID:
          </v-card-text>
          <v-card-text>
            <span
              v-for="portal in this.content.claim.portals"
              :key="portal"
            >
              {{portal}}
            </span>
          </v-card-text>
        </div>
        <div v-else>

        </div>
      </div>

      <v-card-text v-if="this.type == 'claim'">
        
      </v-card-text>

      <v-card-text v-if="this.type == 'edit_paper'">
        
      </v-card-text>

      <v-card-text v-if="this.type == 'modification'">
        
      </v-card-text>

      <v-card-text v-if="this.type == 'new_paper'">
        
      </v-card-text>

      <v-card-text v-if="this.type == 'remove_paper'">
        
      </v-card-text>

      <v-card-text v-if="this.type == 'transfer'">
        
      </v-card-text>

      <div class="d-inline-flex">
        <v-card-text class="font-weight-black">
          申请时间:
        </v-card-text>
        <v-card-text>
          {{message.time}}
        </v-card-text>
      </div>

      <v-divider></v-divider>

      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn
          color="primary"
          text
          @click="dialog = false"
        >
          关闭
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>
export default {
  data() {
    return {
      dialog: false,
      type: "certification",
      content: {
        "claim": {
          "portals": [12,23,34]
        },
        "create": {
          "currentInst": {
            "id": "",
            "name": ""
          },
          "gIndex": 0,
          "hIndex": 0,
          "institutions": [
            {
              "id": "",
              "name": ""
            }
          ],
          "interests": [],
          "name": ""
        }
      }
    }
  },
  props: {
    message: {
      type: Object,
      default:() => {}
    }
  },
  methods: {
    getDetails() {
      this.$axios({
        method: "get",
        url: "/api/account/application/details/"+this.message.id,
      }).then(res => {
        console.log(res.data)
      }).catch(error => {
        console.log(error)
      })
    },
    certification(content) {
      this.type = "certification"
      this.content = content
    },
    /*claim(content) {
      this.type = "claim"
    },
    editPaper(content) {
      this.type = "edit_paper"
    },
    modification(content) {
      this.type = "modification"
    },
    newPaper(content) {
      this.type = "new_paper"
    },
    removePaper(content) {
      this.type = "remove_paper"
    },
    transfer(content) {
      this.type = "transfer"
    }*/
  }
}
</script>

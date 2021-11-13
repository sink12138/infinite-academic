<template>
  <div>
    <h1>Author</h1>
    <Analytics></Analytics>
    <BaseGoBack></BaseGoBack>
  </div>
</template>

<script>
import BaseGoBack from '../components/GoBack.vue';
import Analytics from "../components/Analytics.vue";
export default {
    name: "Income",
    components: {Analytics,BaseGoBack},
    data() {
        return {
        }
    },
    mounted(){
      console.log(1);
    },
    methods:{
    }
}

</script>

<style>

</style>